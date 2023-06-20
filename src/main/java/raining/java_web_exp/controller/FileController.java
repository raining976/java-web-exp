package raining.java_web_exp.controller;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpSession;
import raining.java_web_exp.db.Conn;
import raining.java_web_exp.model.FileEntity;
import raining.java_web_exp.model.User;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/file")
public class FileController {
	private final Conn conn;
	private final String uploadDirPath = "upload/"; // 存储路径

	public FileController() {
		conn = new Conn();
		conn.connDB();
	}

	@PostMapping("/upload")
	public ResponseEntity<String> hanleFileUpload(@RequestParam("files") List<MultipartFile> files,
			@RequestParam("parent_id") int parent_id, @CookieValue("USER_ID") String username, HttpSession session) {
		if (files.size() == 0)
			return ResponseEntity.status(HttpStatus.CONFLICT).body("你还没有上传文件哦");
		User user = conn.getUserByUsername(username);
		if (user == null)
			return ResponseEntity.status(HttpStatus.CONFLICT).body("还没登陆哦");
		// 遍历part的列表
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {
					String originalFilename = StringUtils.cleanPath(file.getOriginalFilename()); // 获取文件名
					Path uploadPath = Path.of(uploadDirPath); // 将上传路径造型为path
					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath); // 上传路径不存在时创建
					}
					Path targetPath = uploadPath.resolve(originalFilename); // 拼接目标路径
					// 在数据库中保存文件信息
					String name = file.getOriginalFilename(); // 获取文件名
					String type = file.getContentType(); // 获取文件类型
					Long size = file.getSize(); // 获取文件大小
					conn.uploadFile(name, type, size, targetPath.toString(), parent_id, user.getId());
					Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING); // 保存文件
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ResponseEntity.ok("上传成功啦！");
	}

	@GetMapping("/filelist")
	public List<FileEntity> getFileList(@CookieValue("USER_ID") String username) {
		User user = conn.getUserByUsername(username);
		if (user == null)
			return new ArrayList<FileEntity>();
		int userId = user.getId();
		return conn.getByUserIdAndParentId(userId);
	}

	/**
	 * 创建目录或txt文件
	 * 
	 * @param pid      父id
	 * @param type     0 为 folder 1 为 txt
	 * @param username 用户名
	 * @return 状态
	 */
	@PostMapping("/newFile")
	public ResponseEntity<String> newFile(@RequestParam("name") String name, @RequestParam("parent_id") int pid,
			@RequestParam("type") int type, @CookieValue("USER_ID") String username) {
		if (username == "")
			return ResponseEntity.status(HttpStatus.CONFLICT).body("出错了哦～");
		int userId = conn.getUserByUsername(username).getId();
		if (type == 0) {
			if (conn.uploadFolder(name, pid, userId))
				return ResponseEntity.ok("目录创建成功了哦");
			else {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("创建失败啦！");
			}
		} else if (type == 1) {
			// 创建txt类型的文件
			String filename = name + ".txt";
			// 创建文本文件
			File uploadDir;
			try {
				// 创建文件
				uploadDir = new File(uploadDirPath);
				if (!uploadDir.exists()) {
					uploadDir.mkdirs();
				}
				File file = new File(uploadDir, filename);
				FileWriter writer = new FileWriter(file);
				writer.write(filename); // 将文件名称写入文件
				writer.close();
				long size = file.length(); // 文件大小
				String filepath = uploadDir + "/" + filename;
				if (conn.uploadFile(filename, "txt", size, filepath, pid, userId)) {
					return ResponseEntity.ok("文本文件创建成功了哦");
				} else {
					return ResponseEntity.status(HttpStatus.CONFLICT).body("创建失败啦！");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body("出错啦！");
	}

	/**
	 * 修改文件（目录的）位置
	 * 
	 * @author raining
	 * @param id       要移动的文件/目录的id
	 * @param pid      移动到某个目录下
	 * @param username 用户名
	 * @return
	 */
	@GetMapping("/changeFilePosition")
	public ResponseEntity<String> changeFilePosition(@RequestParam("id") int id, @RequestParam("pid") int pid,
			@CookieValue("USER_ID") String username) {
		int userId = conn.getUserByUsername(username).getId();
		if (conn.changeFilePid(id, userId, pid))
			return ResponseEntity.ok("修改成功啦");
		return ResponseEntity.status(HttpStatus.CONFLICT).body("修改失败啦！");
	}

	/**
	 * 根据文件id删除文件
	 * 
	 * @param id       文件id
	 * @param username 用户名
	 * @return
	 */
	@GetMapping("/delFiles")
	public ResponseEntity<String> delFiles(@RequestParam("id") int id, @CookieValue("USER_ID") String username) {
		if (username == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("没有登陆哦");
		int userId = conn.getUserByUsername(username).getId();
		if (conn.delFiles(id, userId)) {
			return ResponseEntity.ok("删除成功！");
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body("删除失败啦！");
	}

	/**
	 * 获取文件数据
	 * 
	 * @author raining
	 * @param id      文件id
	 * @param usename 用户名
	 * @return
	 */

	@GetMapping("/fileData")
	public ResponseEntity<byte[]> getFileData(@RequestParam("id") int id, @CookieValue("USER_ID") String usename) {
		int userId = conn.getUserByUsername(usename).getId();
		FileEntity fileEntity = conn.getFileById(id, userId);
		if (fileEntity == null || fileEntity.getType().equals("folder")) {
			return ResponseEntity.notFound().build();
		}
		String filename = fileEntity.getName();
		Path filePath = Paths.get(uploadDirPath, filename);
		Resource resource = new FileSystemResource(filePath);

		if (resource.exists() && resource.isReadable()) {
			byte[] fileData;
			try {
				fileData = Files.readAllBytes(filePath);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(getMediaType(filename));
				headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				headers.set("Content-Disposition", "inline; filename=\"" + filename + "\"");

				return ResponseEntity.ok().headers(headers).body(fileData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// 文件不存在或无法读取时返回404
		return ResponseEntity.notFound().build();

	}

	/**
	 * 根据文件名的后缀返回文件类型
	 * 
	 * @author raining
	 * @param filename 文件名
	 * @return
	 */
	private MediaType getMediaType(String filename) {
		String extension = filename.substring(filename.lastIndexOf(".") + 1);
		switch (extension.toLowerCase()) {
		case "png":
		case "jpg":
		case "jpeg":
			return MediaType.IMAGE_JPEG;
		case "gif":
			return MediaType.IMAGE_GIF;
		case "bmp":
			return MediaType.parseMediaType("image/bmp");
		case "pdf":
			return MediaType.APPLICATION_PDF;
		case "docx":
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		case "xlsx":
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "mp4":
			return MediaType.valueOf("video/mp4");
		case "mp3":
			return MediaType.valueOf("audio/mpeg");
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * 保存修改后的文本文件
	 * @param id 文件id
	 * @param content 文件内容
	 * @param username 用户名
	 * @return
	 */
	@PostMapping("/saveChanges")
	public ResponseEntity<String> saveChanges(@RequestParam("id") int id, @RequestParam("content") String content,
			@CookieValue("USER_ID") String username) {
		int userId = conn.getUserByUsername(username).getId();
		FileEntity fileEntity = conn.getFileById(id, userId);
		
		String filePath = uploadDirPath + fileEntity.getName(); // 替换为实际的文件路径

        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(content);
            writer.flush();
            if (conn.changeUpdatedById(id, userId))
    			return ResponseEntity.ok("修改成功啦");
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body("修改失败啦");

	}

}

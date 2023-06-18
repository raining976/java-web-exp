package raining.java_web_exp.controller;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import raining.java_web_exp.db.Conn;
import raining.java_web_exp.model.FileEntity;
import raining.java_web_exp.model.User;

@RestController
@RequestMapping("/file")
public class FileController {
	private final Conn conn;

	public FileController() {
		conn = new Conn();
		conn.connDB();
	}

	@PostMapping("/upload")
	public ResponseEntity<String> hanleFileUpload(@RequestParam("files") List<MultipartFile> files,
			@RequestParam("parent_id") int parent_id, @CookieValue("USER_ID") String username, HttpSession session) {
		if (files.size() == 0)
			return ResponseEntity.status(HttpStatus.CONFLICT).body("你还没有上传文件哦");
		String uploadDir = "src/main/resources/upload/"; // 存储路径
		User user = conn.getUserByUsername(username);
		if(user == null) return ResponseEntity.status(HttpStatus.CONFLICT).body("还没登陆哦");
		// 遍历part的列表
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {
					String originalFilename = StringUtils.cleanPath(file.getOriginalFilename()); // 获取文件名
					Path uploadPath = Path.of(uploadDir); // 将上传路径造型为path
					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath); // 上传路径不存在时创建
					}
					Path targetPath = uploadPath.resolve(originalFilename); // 拼接目标路径
					// 在数据库中保存文件信息
					conn.uploadFile(targetPath.toString(), parent_id, user.getId(), file);
					Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING); // 保存文件
				} catch (IOException e) {
					return ResponseEntity.status(HttpStatus.CONFLICT).body("出错了哦～");

				}
			}
		}
		return ResponseEntity.ok("上传成功啦！");
	}
	
	@GetMapping("/filelist")
	public List<FileEntity> getFileList (@CookieValue("USER_ID") String username){
		User user = conn.getUserByUsername(username);
		if(user == null) return new ArrayList<FileEntity>();
		int userId = user.getId();
		return conn.getByUserIdAndParentId(userId);
	}
	
	 // 创建目录或txt文件
	/**
	 * 
	 * @param pid 父id
	 * @param type 0 为 folder 1 为 txt
	 * @param username 用户名
	 * @return 状态
	 */
	@PostMapping("/newFile")
	public ResponseEntity<String> newFile(@RequestParam("parent_id") int pid, @RequestParam("type") int type,@CookieValue("USER_ID") String username){
		if(username ==null) return ResponseEntity.status(HttpStatus.CONFLICT).body("出错了哦～");
		return ResponseEntity.ok("success");
	}
	

}

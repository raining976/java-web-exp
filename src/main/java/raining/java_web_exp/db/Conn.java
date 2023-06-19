package raining.java_web_exp.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;

import raining.java_web_exp.model.FileEntity;
import raining.java_web_exp.model.User;

public class Conn {
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	PreparedStatement ps = null;

	// 创建对象的同时连接数据库，以便下边的函数使用
	public Conn() {
		connDB();
	}

	// 连接数据库
	public void connDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// 数据库配置
		String url = "jdbc:mysql://localhost:3306/java-web";
		String username = "root";
		String password = "admin123";

		try {
			con = DriverManager.getConnection(url, username, password);
			System.out.println("Database Opened.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database Connection failed.");
		}

	}

	// 关闭数据库
	public void closeDB() throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
		if (stmt != null) {
			stmt.close();
			stmt = null;
		}
		if (con != null) {
			con.close();
			con = null;
		}
		System.out.println("Database Closed.");
	}

	// 查询操作封装
	public ResultSet SelectedSql(String sql) throws SQLException {
		if (sql == null || sql.equals(" "))
			return null;
		sql = sql.trim();// 去掉字符串两边的空格
		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = stmt.executeQuery(sql);
		return rs;
	}

	public boolean UpdateSQL(String sql) {
		boolean flag = false;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
			flag = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;

	}

	// 通过用户名获取用户信息
	public User getUserByUsername(String name) {

		User user = null;
		String str = "select * from user where name = '" + name + "'";
		try {
			rs = this.SelectedSql(str);
			if (rs.next()) {
				// TODO: 处理查询到的数据
				int id = rs.getInt("id");
				String username = rs.getString("name");
				String password = rs.getString("password");
				String imgUrl = rs.getString("img_url");
				String createDate = rs.getString("create_date");
				user = new User(id, username, password, imgUrl, createDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	// 注册新用户
	public boolean registerUser(User user) {
		if (this.getUserByUsername(user.getUsername()) == null) {
			String str = "insert into user (name, password, img_url, create_date) VALUES ('" + user.getUsername()
					+ "', '" + user.getPassword() + "' , '" + user.getImgUrl() + "','" + user.getCreateDate() + "');";
			this.UpdateSQL(str);
			return true;
		}
		return false;
	}

	// 上传文件信息到数据库
	public boolean uploadFile(String name,String type,Long size,String filePath, int pid, int userId) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "insert into file (user_id, parent_id,name,type,size,path,updated) values('" + userId + "','"
				+ pid + "','" + name + "','" + type + "','" + size + "','" + filePath + "','" + formattedDateTime
				+ "')";
		return this.UpdateSQL(strSql);
	}

	// 创建文件夹
	public boolean uploadFolder(String name, int pid, int userId) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "insert into file (user_id,parent_id,name,type,size,path,updated) values ('"+userId+"','"+pid+"','"+name+"','folder',0,'-','"+formattedDateTime+"')";
		if(this.UpdateSQL(strSql)) return true;
		return false;
	}

	// 根据user id 和 parent id 获取 文件列表
	public List<FileEntity> getByUserIdAndParentId(int userId) {
		List<FileEntity> filelist = new ArrayList<FileEntity>();
		String strSql = "select * from file where user_id = '" + userId + "'";
		try {
			rs = this.SelectedSql(strSql);
			while (rs.next()) {
				// 获取文件信息并创建对应entity
				int id = rs.getInt("id");
				int user_id = rs.getInt("user_id");
				int parent_id = rs.getInt("parent_id");
				String name = rs.getString("name");
				String type = rs.getString("type");
				Long size = rs.getLong("size");
				String path = rs.getString("path");
				String date = rs.getString("updated");
				FileEntity fileEntity = new FileEntity(id, user_id, parent_id, name, type, size, path, date);
				filelist.add(fileEntity);
			}
			return filelist;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return filelist;
	}
	
	// 修改文件的parent id
	public boolean changeFilePid(int id,int userId,int pid) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "update file set parent_id = '"+pid+"' where user_id = '"+userId+"' and id = '"+id+"'";
		String updateSql = "update file set updated='"+formattedDateTime+"' where id = '"+pid+"'";
		this.UpdateSQL(updateSql);
		return this.UpdateSQL(strSql);
	}
}

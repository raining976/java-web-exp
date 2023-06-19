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

	/**
	 * 封装查询操作
	 * @param sql sql语句
	 * @return
	 * @throws SQLException
	 */
	public ResultSet SelectedSql(String sql) throws SQLException {
		if (sql == null || sql.equals(" "))
			return null;
		sql = sql.trim();// 去掉字符串两边的空格
		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		rs = stmt.executeQuery(sql);
		return rs;
	}

	/**
	 * 封装执行update insert语句的函数 
	 * @param sql sql语句
	 * @return
	 */
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

	/**
	 * 通过用户名获取用户的model
	 * @author raining
	 * @param name 用户名
	 * @return
	 */
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

	/**
	 * 注册用户
	 * @author raining
	 * @param user 用户model
	 * @return
	 */
	public boolean registerUser(User user) {
		if (this.getUserByUsername(user.getUsername()) == null) {
			String str = "insert into user (name, password, img_url, create_date) VALUES ('" + user.getUsername()
					+ "', '" + user.getPassword() + "' , '" + user.getImgUrl() + "','" + user.getCreateDate() + "');";
			this.UpdateSQL(str);
			return true;
		}
		return false;
	}

	/**
	 * 将文件信息存入数据库
	 * @author raining
	 * @param name 文件名
	 * @param type 文件类型
	 * @param size 文件大小
	 * @param filePath 文件路径
	 * @param pid 父id
	 * @param userId 用户id
	 * @return
	 */
	public boolean uploadFile(String name,String type,Long size,String filePath, int pid, int userId) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "insert into file (user_id, parent_id,name,type,size,path,updated) values('" + userId + "','"
				+ pid + "','" + name + "','" + type + "','" + size + "','" + filePath + "','" + formattedDateTime
				+ "')";
		return this.UpdateSQL(strSql);
	}

	/**
	 * 将创建的文件夹信息存储数据库
	 * @author raining
	 * @param name 文件名
	 * @param pid parent_id
	 * @param userId 用户id
	 * @return
	 */
	public boolean uploadFolder(String name, int pid, int userId) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "insert into file (user_id,parent_id,name,type,size,path,updated) values ('"+userId+"','"+pid+"','"+name+"','folder',0,'-','"+formattedDateTime+"')";
		if(this.UpdateSQL(strSql)) return true;
		return false;
	}

	/**
	 * 获取当前用户的所有列表
	 * @author raining
	 * @param userId 用户id
	 * @return
	 */
	public List<FileEntity> getByUserIdAndParentId(int userId) {
		List<FileEntity> filelist = new ArrayList<FileEntity>();
		String strSql = "select * from file where user_id = '" + userId + "' and status = 0";
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
	
	/**
	 * 更改文件位置 实际上是更新文件的pid
	 * @param id 文件 id
	 * @param userId 用户 id
	 * @param pid parent id
	 * @return
	 */
	public boolean changeFilePid(int id,int userId,int pid) {
		LocalDateTime now = LocalDateTime.now(); // 获取当前时间
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"); // 创建日期时间格式化器
		String formattedDateTime = now.format(formatter); // 格式化当前时间为字符串
		String strSql = "update file set parent_id = '"+pid+"' where user_id = '"+userId+"' and id = '"+id+"'";
		String updateSql = "update file set updated='"+formattedDateTime+"' where id = '"+pid+"'";
		this.UpdateSQL(updateSql);
		return this.UpdateSQL(strSql);
	}
	
	/**
	 * 用 update 修改status代替彻底删除操作，更安全 
	 * @param id 文件/文件夹 id
	 * @param uid 用户 id
	 * @return 
	 */
	public boolean delFiles(int id,int uid) {
		String str = "update file set status = 1 where id = '"+id+"' and user_id = '"+uid+"'";
		return this.UpdateSQL(str);
	}
}

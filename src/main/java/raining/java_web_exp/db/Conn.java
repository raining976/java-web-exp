package raining.java_web_exp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import raining.java_web_exp.model.User;

public class Conn {
	private Connection con = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	PreparedStatement ps = null;

	// 创建对象的同时连接数据库，以便下边的函数使用
	public Conn(){
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

	public void UpdateSQL(String sql) {
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public boolean isUserExist(String username) {
		if (username == null || username.equals(" "))
			return false;
		String sqlStr = "select count(*) from user where username = ?";

		try {
			stmt = con.createStatement();
			ps = con.prepareStatement(sqlStr);
			ps.setString(1, username);
			rs = ps.executeQuery();

			if (rs.getInt(1) > 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public User getUserByUsername(String name) {
		
		User user = null;
		String str = "select * from user where name = '"+name+"'";
		try {
			rs = this.SelectedSql(str);
			if(rs.next()) {
				// TODO: 处理查询到的数据
				int id = rs.getInt("id");
				String username = rs.getString("name");
				String password = rs.getString("password");
				String imgUrl = rs.getString("img_url");
				String createDate = rs.getString("create_date");
				user = new User(id,username,password,imgUrl,createDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

}

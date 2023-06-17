package raining.java_web_exp.model;
import javax.persistence.*;

public class User {
    private int id;
	
	private String username;
	private String password;
	private String createDate;
	private String imgUrl;
	public User() {}
	public User(int id,String name, String pass, String img,String date) {
		this.id = id;
		username = name;
		password = pass;
		imgUrl = img;
		createDate = date;
	}
	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getCreateDate() {
		return createDate;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
}

package raining.java_web_exp.model;

public class FileEntity {
	private int id;
	private int user_id;
	private int parent_id;
	private String name;
	private String type;
	private Long size;
	private String path;
	private String updated;
	
	public FileEntity() {}
	public FileEntity(int id, int userId,int pid,String name , String type,Long size, String path,String date) {
		this.id  = id;
		user_id = userId;
		parent_id = pid;
		this.name = name;
		this.type = type;
		this.size = size;
		this.path = path;
		this.updated = date;
	}
	
	public int getId() {
		return id;
	}
	public int getUser_id() {
		return user_id;
	}
	public int getParent_id() {
		return parent_id;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public Long getSize() {
		return size;
	}
	public String getPath() {
		return path;
	}
	public String getUpdated() {
		return updated;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public void setUser_id(int userId) {
		this.user_id = userId;
	}
	public void setParent_id(int pid) {
		parent_id = pid;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
}

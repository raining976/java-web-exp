package raining.java_web_exp.controller;

import jakarta.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import raining.java_web_exp.model.*;
import raining.java_web_exp.db.Conn;
@RestController
@RequestMapping("/user")
public class UserController {
	private Conn conn = new Conn();
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody  User user, HttpSession session, HttpServletResponse res) {
		String formPass = user.getPassword();
		User userEntity = conn.getUserByUsername(user.getUsername());
		if (userEntity != null && userEntity.getPassword().equals(formPass)) {
			session.setAttribute("USER_INFO", user);
			Cookie cookie = new Cookie("USER_ID",user.getUsername());
			int maxAge = 10*60;
			cookie.setMaxAge(maxAge); // 有效期
			cookie.setPath("/"); // 设置Cookie的作用范围为整个应用程序
			Cookie expireCookie = new Cookie("MAX_AGE","600"); // 过期时间的cookie 
			expireCookie.setMaxAge(maxAge); // 有效期
			expireCookie.setPath("/"); // 设置Cookie的作用范围为整个应用程序
			res.addCookie(cookie);
			res.addCookie(expireCookie);
			return ResponseEntity.ok("success");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("fail");
	}
	
	@GetMapping("/info")
	public User userinfo(@RequestParam("name") String username, HttpSession session) {
		return conn.getUserByUsername(username);
	}
	
	
//	@GetMapping("/userList")
//	public List<UserEntity> sayHello() {
//
//		UserEntity userEntity = new UserEntity();
//
//		userEntity.setUsername("Xiaodong");
//		userEntity.setPassword("123456");
//		userEntity.setCreateDate("today");
//
//		List<UserEntity> userEntities = new ArrayList<UserEntity>();
//		userEntities.add(userEntity);
//
//		return userEntities;
//	}
}

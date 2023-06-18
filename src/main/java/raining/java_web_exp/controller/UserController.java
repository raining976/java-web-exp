package raining.java_web_exp.controller;

import jakarta.servlet.http.Cookie;

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
	
	// 登陆接口
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody User user, HttpSession session, HttpServletResponse res) {
		String formPass = user.getPassword();
		User userEntity = conn.getUserByUsername(user.getUsername());
		if (userEntity != null && userEntity.getPassword().equals(formPass)) {
			session.setAttribute("USER_INFO", userEntity);
			Cookie cookie = new Cookie("USER_ID", user.getUsername());
			int maxAge = 10 * 60;
			cookie.setMaxAge(maxAge); // 有效期
			cookie.setPath("/"); // 设置Cookie的作用范围为整个应用程序
			Cookie expireCookie = new Cookie("MAX_AGE", "600"); // 过期时间的cookie
			expireCookie.setMaxAge(maxAge); // 有效期
			expireCookie.setPath("/"); // 设置Cookie的作用范围为整个应用程序
			res.addCookie(cookie);
			res.addCookie(expireCookie);
			return ResponseEntity.ok("登陆成功啦！");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("密码不对哦");
	}

	// 注册接口
	@PostMapping("/register")
	public ResponseEntity<String> login(@RequestBody User user, HttpSession session) {
		boolean flag  = this.conn.registerUser(user);
		if(flag) {
			return ResponseEntity.ok("注册成功啦！");
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已被注册了哦");
	}

	// 获取用户信息接口
	@GetMapping("/info")
	public User userinfo(@RequestParam("name") String username, HttpSession session) {
		return conn.getUserByUsername(username);
	}

	
}

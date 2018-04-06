package com.example.sso;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/user")
	public String user() {
		return "user";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/403")
	public String accessDenied() {
		return "403";
	}
}

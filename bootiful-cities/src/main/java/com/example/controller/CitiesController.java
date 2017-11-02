package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class CitiesController {

	@Value("${greeting:Hola}")
	private String greeting;

	@RequestMapping("/")
	public String hello() {
		return String.join(" ", greeting, "Cities World!!!");
	}

}
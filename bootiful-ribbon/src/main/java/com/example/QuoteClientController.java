package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@RibbonClient(name = "bootiful-quotes", configuration = LBConfiguration.class)
@Slf4j
public class QuoteClientController {
	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;

	@RequestMapping("/quote")
	public String quote() {
		final String quote = this.restTemplate.getForObject("http://bootiful-quotes/api/random", String.class);
		log.info("Got quote : {}", quote);
		return quote;
	}

	@RequestMapping("/quote/{id}")
	public String quoteById(@PathVariable(value = "id") long id) {
		final String quote = this.restTemplate.getForObject("http://bootiful-quotes/api/" + id, String.class);
		log.info("Got quote : {}", quote);
		return quote;
	}
}

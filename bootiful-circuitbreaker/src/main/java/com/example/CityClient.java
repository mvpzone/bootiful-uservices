package com.example;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.domain.City;

//@FeignClient(name = "bootiful-cities", fallback = CityClientFallback.class)
@FeignClient(name = "bootiful-cities", fallbackFactory = CityClientFallbackFactory.class)
public interface CityClient {

	@GetMapping(value = "/cities", consumes = "application/hal+json")
	Resources<City> getCities();
}
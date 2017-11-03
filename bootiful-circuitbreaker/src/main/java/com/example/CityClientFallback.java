package com.example;

import java.util.Collections;

import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;

import com.example.domain.City;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

@Component
public class CityClientFallback implements CityClient {

	@Override
	public Resources<City> getCities() {
		// We'll just return an empty response
		return new Resources<City>(Collections.emptyList());
	}
}

@Component
@Slf4j
class CityClientFallbackFactory implements FallbackFactory<CityClient> {

	@Override
	public CityClient create(final Throwable t) {
		return new CityClient() {
			@Override
			public Resources<City> getCities() {
				log.info("Fallback triggered by {} due to {}", t.getClass().getName(), t.getMessage());
				return new Resources<City>(Collections.emptyList());
			}
		};
	}
}
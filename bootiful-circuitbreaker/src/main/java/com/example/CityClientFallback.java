package com.example;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;

import com.example.domain.City;

import feign.hystrix.FallbackFactory;

@Component
public class CityClientFallback implements CityClient {

	@Override
	public Resources<City> getCities() {
		// We'll just return an empty response
		return new Resources<City>(Collections.emptyList());
	}
}

@Component
class CityClientFallbackFactory implements FallbackFactory<CityClient> {
	private static final Logger LOG = LoggerFactory.getLogger(CityClientFallbackFactory.class);

	@Override
	public CityClient create(final Throwable t) {
		return new CityClient() {
			@Override
			public Resources<City> getCities() {
				LOG.info("Fallback triggered by {} due to {}", t.getClass().getName(), t.getMessage());
				return new Resources<City>(Collections.emptyList());
			}
		};
	}
}
package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class UIConfig {

	@Bean
	public HalHttpMessageConverter halHttpMessageConverter() {
		return new HalHttpMessageConverter();
	}
}

class HalHttpMessageConverter extends AbstractJackson2HttpMessageConverter {
	public HalHttpMessageConverter() {
		super(new ObjectMapper(), new MediaType("application", "hal+json", DEFAULT_CHARSET));
		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.setHandlerInstantiator(
				new Jackson2HalModule.HalHandlerInstantiator(new DefaultRelProvider(), null, null));
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return ResourceSupport.class.isAssignableFrom(clazz);
	}
}
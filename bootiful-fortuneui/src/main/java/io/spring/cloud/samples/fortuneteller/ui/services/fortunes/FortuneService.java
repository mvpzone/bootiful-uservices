package io.spring.cloud.samples.fortuneteller.ui.services.fortunes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
@EnableConfigurationProperties(FortuneProperties.class)
public class FortuneService {

	@Autowired
	FortuneProperties fortuneProperties;

	@Autowired
	RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "fallbackFortune")
	public Fortune randomFortune() {
		return restTemplate.getForObject("http://fortunes/random", Fortune.class);
	}

	@SuppressWarnings(value = { "unused" })
	private Fortune fallbackFortune() {
		return new Fortune(42L, fortuneProperties.getFallbackFortune());
	}
}

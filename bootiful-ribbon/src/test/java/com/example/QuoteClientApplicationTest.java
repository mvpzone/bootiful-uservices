package com.example;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuoteClientApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuoteClientApplicationTest {

	ConfigurableApplicationContext application1;
	ConfigurableApplicationContext application2;

	@Before
	public void startApps() {
		this.application1 = startApp(8080);
		this.application2 = startApp(9080);
	}

	@After
	public void closeApps() {
		this.application1.close();
		this.application2.close();
	}

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void shouldRoundRobinOverInstancesWhenCallingServicesViaRibbon() throws InterruptedException {
		ResponseEntity<String> response1 = this.testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/quote", String.class);
		ResponseEntity<String> response2 = this.testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/quote/1", String.class);

		then(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
		then(response1.getBody()).isEqualTo("1");
		then(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
		then(response2.getBody()).isEqualTo("2");
	}

	private ConfigurableApplicationContext startApp(int port) {
		return SpringApplication.run(TestApplication.class, "--server.port=" + port, "--spring.jmx.enabled=false");
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	static class TestApplication {
		static AtomicInteger atomicInteger = new AtomicInteger();

		@RequestMapping(value = "/api/random")
		public Integer quote() {
			return atomicInteger.incrementAndGet();
		}

		@RequestMapping(value = "/api/1")
		public Integer quoteById() {
			return atomicInteger.incrementAndGet();
		}

		@RequestMapping(value = "/")
		public String health() {
			return "ok";
		}
	}
}

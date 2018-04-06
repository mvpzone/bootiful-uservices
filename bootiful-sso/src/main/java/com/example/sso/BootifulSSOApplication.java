package com.example.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BootifulSSOApplication {

	public static void main(String[] args) {
		if ("true".equals(System.getenv("SKIP_SSL_VALIDATION"))) {
			SecurityConfig.SSLValidationDisabler.disableSSLValidation();
		}

		SpringApplication.run(BootifulSSOApplication.class, args);
	}
}

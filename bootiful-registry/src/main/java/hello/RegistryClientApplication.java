package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class RegistryClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistryClientApplication.class, args);
	}
}

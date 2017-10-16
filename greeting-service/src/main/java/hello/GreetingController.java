package hello;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!!!";
	}

	@RequestMapping("/greeting")
	public String greeting() {
		return "Hello World";
	}
}

package bootiful.quotes;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class DataInitializer implements ApplicationRunner {

	@Autowired
	private QuoteRepository quoteRepository;

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		Stream.of("Working with Spring Boot is like pair-programming with the Spring developers.",
				"With Boot you deploy everywhere you can find a JVM basically.",
				"Spring has come quite a ways in addressing developer enjoyment and "
						+ "ease of use since the last time I built an application using it.",
				"Previous to Spring Boot, I remember XML hell, confusing set up, and many hours of frustration.",
				"Spring Boot solves this problem. It gets rid of XML and wires up "
						+ "common components for me, so I don't have to spend hours scratching my "
						+ "head just to figure out how it's all pieced together.",
				"It embraces convention over configuration, providing an experience on par with "
						+ "frameworks that excel at early stage development, such as Ruby on Rails.",
				"The real benefit of Boot, however, is that it's just Spring. That "
						+ "means any direction the code takes, regardless of complexity, I know it's a safe bet.",
				"I don't worry about my code scaling. Boot allows the "
						+ "developer to peel back the layers and customize when it's appropriate "
						+ "while keeping the conventions that just work.",
				"So easy it is to switch container in #springboot.",
				"Really loving Spring Boot, makes stand alone Spring apps easy.",
				"I have two hours today to build an app from scratch. @springboot to the rescue!",
				"@springboot with @springframework is pure productivity! Who said in #java one has "
						+ "to write double the code than in other langs? #newFavLib")
				.forEach(quote -> quoteRepository.save(new Quote(quote)));

		quoteRepository.findAll().forEach(System.out::println);
	}
}
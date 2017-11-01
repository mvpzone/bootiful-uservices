package bootiful.quotes;

import java.util.Objects;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {
	private static final Logger LOG = LoggerFactory.getLogger(QuoteController.class);

	@Autowired
	private QuoteRepository repository;
	private final static Random RANDOMIZER = new Random();

	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
	public ResponseEntity<Quote> getOne(@PathVariable Long id) {
		if (Objects.isNull(id)) {
			return new ResponseEntity<>(Quote.NONE, HttpStatus.NOT_FOUND);
		}

		final Quote quote = repository.findOne(id);
		if (Objects.isNull(quote)) {
			return new ResponseEntity<>(Quote.NONE, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(quote, HttpStatus.OK);
	}

	@RequestMapping(value = "/api/random", method = RequestMethod.GET)
	public Quote getRandomOne() {
		final Quote quote = repository.findOne(nextLong(1, repository.count() + 1));
		LOG.info("Returning Quote : {}", quote);
		return quote;
	}

	private long nextLong(long lowerRange, long upperRange) {
		return (long) (RANDOMIZER.nextDouble() * (upperRange - lowerRange)) + lowerRange;
	}

}
package bootiful.quotes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
@Entity
@Table(name = "QUOTES")
public class Quote {
	protected final static Quote NONE = new Quote("None");

	@Id
	@GeneratedValue
	Long id;

	@Column(nullable = false)
	private String quote;

	public Quote(final String quote) {
		this.quote = quote;
	}
}

package top.chancelethay.bingo.data.record;

import java.util.Set;

public record BingoCard(String cardName, Set<String> excludedTags) {

	public BingoCard(String cardName) {
		this(cardName, Set.of());
	}
}

package com.danny.fish.model;

import com.danny.fish.constants.CardRank;
import com.danny.fish.constants.CardSuit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Card {
	private CardSuit suit;
	private CardRank rank;

	public String toString() {
		return rank + " of " + suit;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Card card = (Card) obj;
		return this.suit == card.suit && this.rank == card.rank;
	}
}

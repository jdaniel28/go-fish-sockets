package com.danny.fish.constants;

public enum CardRank {

	TWO("2"),

	THREE("3"),

	FOUR("4"),

	FIVE("5"),

	SIX("6"),

	SEVEN("7"),

	EIGHT("8"),

	NINE("9"),

	TEN("T"),

	JACK("J"),

	QUEEN("Q"),

	KING("K"),

	ACE("A");

	private final String display;

	private CardRank(String display) {
		this.display = display;
	}

	public String getDisplay() {
		return this.display;
	}
}

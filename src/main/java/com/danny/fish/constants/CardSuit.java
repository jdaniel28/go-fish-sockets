package com.danny.fish.constants;

public enum CardSuit {
	HEARTS("H"), DIAMONDS("D"), CLUBS("C"), SPADES("S");

	private final String display;

	private CardSuit(String display) {
		this.display = display;
	}

	public String getDisplay(){
		return this.display;
	}
}

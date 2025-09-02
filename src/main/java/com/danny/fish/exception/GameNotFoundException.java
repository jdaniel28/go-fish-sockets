package com.danny.fish.exception;

public class GameNotFoundException extends RuntimeException {
	public GameNotFoundException(String message) {
		super(message);
	}

	public GameNotFoundException(){
		super("Game not found.");
	}
}

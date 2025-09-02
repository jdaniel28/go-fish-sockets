package com.danny.fish.exception;

public class PlayerNotFoundException extends RuntimeException {
	public PlayerNotFoundException(String message) {
		super(message);
	}

    public PlayerNotFoundException(){
      super("Player not found.");
    }
}

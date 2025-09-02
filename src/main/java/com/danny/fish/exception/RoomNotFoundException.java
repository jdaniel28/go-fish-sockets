package com.danny.fish.exception;

public class RoomNotFoundException extends RuntimeException {
	public RoomNotFoundException(String message) {
		super(message);
	}

	public RoomNotFoundException(){
		super("Room not found.");
	}
}

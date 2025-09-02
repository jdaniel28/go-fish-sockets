package com.danny.fish.model.event;

import com.danny.fish.model.GameRoom;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoomUpdatedEvent extends ApplicationEvent {

	private GameRoom gameRoom;

	private String roomUpdatedEvent;

	public RoomUpdatedEvent(Object source) {
		super(source);
	}

	public RoomUpdatedEvent(Object source, GameRoom gameRoom) {
		super(source);
		this.gameRoom = gameRoom;
	}

	public RoomUpdatedEvent(Object source, GameRoom gameRoom, String roomUpdatedEvent) {
		super(source);
		this.gameRoom = gameRoom;
		this.roomUpdatedEvent = roomUpdatedEvent;
	}
}

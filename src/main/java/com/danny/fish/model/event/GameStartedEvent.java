package com.danny.fish.model.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class GameStartedEvent extends ApplicationEvent {
	private Long roomId;

	public GameStartedEvent(Object source) {
		super(source);
	}

	public GameStartedEvent(Object source, Long roomId) {
		super(source);
		this.roomId = roomId;
	}
}

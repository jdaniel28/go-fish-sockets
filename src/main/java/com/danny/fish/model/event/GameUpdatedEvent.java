package com.danny.fish.model.event;

import com.danny.fish.model.PlayerGameView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class GameUpdatedEvent extends ApplicationEvent {

	private PlayerGameView playerGameView;
	private String eventOccurred;

	public GameUpdatedEvent(Object source) {
		super(source);
	}

	public GameUpdatedEvent(Object source, PlayerGameView playerGameView) {
		super(source);
		this.playerGameView = playerGameView;
	}

	public GameUpdatedEvent(Object source, PlayerGameView playerGameView, String eventOccurred) {
		super(source);
		this.playerGameView = playerGameView;
		this.eventOccurred = eventOccurred;
	}
}

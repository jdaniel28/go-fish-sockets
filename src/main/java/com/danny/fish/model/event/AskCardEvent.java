package com.danny.fish.model.event;

import com.danny.fish.model.PlayerRequestCard;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AskCardEvent extends ApplicationEvent {

	private PlayerRequestCard playerRequestCard;

	public AskCardEvent(Object source) {
		super(source);
	}

	public AskCardEvent(Object source, PlayerRequestCard playerRequestCard) {
		super(source);
		this.playerRequestCard = playerRequestCard;
	}
}

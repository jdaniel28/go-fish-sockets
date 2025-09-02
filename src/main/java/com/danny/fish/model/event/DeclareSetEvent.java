package com.danny.fish.model.event;

import com.danny.fish.model.PlayerDeclareRank;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DeclareSetEvent extends ApplicationEvent {
	private PlayerDeclareRank playerDeclareRank;

	public DeclareSetEvent(Object source) {
		super(source);
	}

	public DeclareSetEvent(Object souce, PlayerDeclareRank playerDeclareRank) {
		super(souce);
		this.playerDeclareRank = playerDeclareRank;
	}
}

package com.danny.fish.model.event;

import com.danny.fish.model.request.AddPlayerToRoomRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AddPlayerEvent extends ApplicationEvent {

	private AddPlayerToRoomRequest addPlayerToRoomRequest;

	public AddPlayerEvent(Object source) {
		super(source);
	}

	public AddPlayerEvent(Object source, AddPlayerToRoomRequest addPlayerToRoomRequest) {
		super(source);
		this.addPlayerToRoomRequest = addPlayerToRoomRequest;
	}
}

package com.danny.fish.model.event;

import com.danny.fish.model.request.CreateRoomRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreateGameEvent extends ApplicationEvent {

	private CreateRoomRequest createRoomRequest;

	public CreateGameEvent(Object source) {
		super(source);
	}

	public CreateGameEvent(Object source, CreateRoomRequest createRoomRequest) {
		super(source);
		this.createRoomRequest = createRoomRequest;
	}
}

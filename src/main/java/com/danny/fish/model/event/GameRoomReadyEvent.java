package com.danny.fish.model.event;

import com.danny.fish.model.GameRoom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class GameRoomReadyEvent extends ApplicationEvent {
	private Long roomId;
	private GameRoom gameRoom;
	private List<Long> playerIds;

	public GameRoomReadyEvent(Object source) {
		super(source);
	}

	public GameRoomReadyEvent(Object source, Long roomId, List<Long> playerIds, GameRoom gameRoom) {
		super(source);
		this.roomId = roomId;
		this.playerIds = playerIds;
		this.gameRoom = gameRoom;
	}
}

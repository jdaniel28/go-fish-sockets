package com.danny.fish.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameRoomView {
	private long roomId;
	private String roomCode;
	private String eventOccurred;
	private PlayerGameView playerGameView;
	private Map<Long, PlayerGameView.PlayerInfo> playerInfoMap; // playerId to playerName
	private int totalPlayers;
}

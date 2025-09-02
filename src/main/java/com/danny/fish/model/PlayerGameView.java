package com.danny.fish.model;

import com.danny.fish.constants.CardRank;
import com.danny.fish.constants.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGameView {
	private long playerId;
	private long gameId;
	private List<Card> playerCards;
	private List<Game.GameLogEntry> gameLogEntries;
	private long currentPlayerTurnId;
	private GameStatus gameStatus;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlayerInfo {
		private long playerId;
		private String playerName;
		private List<CardRank> declaredRanks;
	}
}

package com.danny.fish.model;

import com.danny.fish.constants.GameStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Game {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private GameStatus gameStatus;

	@ManyToOne
	@JoinColumn(name = "gameRoomId", referencedColumnName = "id")
	private GameRoom gameRoom;

	@OneToMany(
			mappedBy = "game",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	@OrderBy("playerTurnId ASC")
	private List<GamePlayerHand> playerHands;
	@OneToMany(
			mappedBy = "gameId",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	@OrderBy("timestampMillis ASC")
	private List<GameLogEntry> gameLogEntries;
	private Long playerToPlayId;
	@Column(length = 10000)
	private String deck;
//	private String logHistory;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Entity
	public static class GamePlayerHand {
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private Long id;
		private Long playerTurnId;

		@ManyToOne
		@JoinColumn(name = "gameId", nullable = false)
		private Game game;
		private Long playerId;
		@Column(length = 10000)
		private String hand; // JSON representation of List<Card>
		@Column(length = 2000)
		private String declaredRanks = "[]"; // JSON representation of List<CardRank>
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Entity
	public static class GameLogEntry{
		@Id
		private Long id;
		@ManyToOne
		@JoinColumn(name = "gameId", nullable = false)
		private Game gameId;
		@Column(length = 10000)
		private String logEntry;
		private long timestampMillis;
	}
}

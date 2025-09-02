package com.danny.fish.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToMany(
			mappedBy = "roomId",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<AssignedPlayers> assignedPlayersIds = new ArrayList<>();
	private Integer totalPlayerCount;
	private Long hostPlayerId;
	private boolean gameStarted = false;
	private boolean gameCompleted = false;
	private Long gameWinnerId;
	private String joinCode;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Entity
	public static class AssignedPlayers{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private Long id;
		@ManyToOne
		@JoinColumn(name = "roomId")
		private GameRoom roomId;
		private Long playerId;
	}

	public void assignPlayer(Long playerId) {
		AssignedPlayers assignedPlayer = new AssignedPlayers();
		assignedPlayer.setRoomId(this);
		assignedPlayer.setPlayerId(playerId);
		this.assignedPlayersIds.add(assignedPlayer);
	}

	public boolean removePlayer(Long playerId) {
		return this.assignedPlayersIds.removeIf(ap -> ap.getPlayerId().equals(playerId));
	}
}

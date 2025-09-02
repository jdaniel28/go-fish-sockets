package com.danny.fish.repo;

import com.danny.fish.model.GameRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoomDao extends JpaRepository<GameRoom, Long> {

	@EntityGraph(attributePaths = {"assignedPlayersIds"})
	public GameRoom findByJoinCode(String joinCode);
}

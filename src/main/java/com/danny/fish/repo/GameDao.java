package com.danny.fish.repo;

import com.danny.fish.model.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameDao extends JpaRepository<Game, Long> {

	@EntityGraph(attributePaths = {"playerHands", "gameLogEntries", "gameRoom"})
	Optional<Game> findById(Long id);
}

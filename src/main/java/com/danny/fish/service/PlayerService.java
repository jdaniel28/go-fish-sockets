package com.danny.fish.service;

import com.danny.fish.model.Player;
import com.danny.fish.repo.PlayerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlayerService {

	private final PlayerDao playerDao;

	public PlayerService(PlayerDao playerDao){
		this.playerDao = playerDao;
	}

	public Player createPlayer(String playerName){
		log.debug("Creating player with name: {}", playerName);
		Player player = new Player();
		player.setPlayerName(playerName);
		return playerDao.save(player);
	}
}

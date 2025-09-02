package com.danny.fish.controller;

import com.danny.fish.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class PlayerController {

	private final PlayerService playerService;

	@Autowired
	public PlayerController(PlayerService playerService) {
		this.playerService = playerService;
	}

	@PostMapping("/")
	public ResponseEntity<Object> createPlayer(@RequestBody String playerName) {
		return ResponseEntity.ok(playerService.createPlayer(playerName));
	}
}

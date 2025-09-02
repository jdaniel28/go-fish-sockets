package com.danny.fish.controller;

import com.danny.fish.model.request.AddPlayerToRoomRequest;
import com.danny.fish.model.request.CreateRoomRequest;
import com.danny.fish.service.GameRoomService;
import com.danny.fish.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
//@CrossOrigin(origins = { "http://localhost:4200", "http://nggofish.s3-website.ap-south-1.amazonaws.com" })
public class GameController {

	@Autowired
	private GameService gameService;

	@Autowired
	private GameRoomService gameRoomService;

	@PostMapping("/game-rooms")
	public ResponseEntity<Object> createGameRoom(@RequestBody CreateRoomRequest request) {
		return new ResponseEntity<>(gameRoomService.createGameRoom(request.getPlayerCount(), request.getHostId()), HttpStatus.CREATED);
	}

	@PostMapping("/game-rooms/{roomId}/players")
	public ResponseEntity<Object> joinGameRoom(@PathVariable String roomId, @RequestBody AddPlayerToRoomRequest request) {
		return new ResponseEntity<>(gameRoomService.addPlayerToRoom(roomId, request.getPlayerId()), HttpStatus.OK);
	}

	@DeleteMapping("/game-rooms/{roomId}/players/{playerId}")
	public ResponseEntity<Object> leaveGameRoom(@PathVariable("roomId") String roomId, @PathVariable("playerId") Long playerId) {
		return new ResponseEntity<>(gameRoomService.removePlayerFromRoom(roomId, playerId), HttpStatus.OK);
	}

	@GetMapping("/game-rooms/{roomId}/state")
	public ResponseEntity<Object> getGameState(){
		return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
	}

	@MessageMapping("/join-room")
	public void joinGameRoom(AddPlayerToRoomRequest request) {
		this.gameRoomService.addPlayerToRoom(request.getRoomCode(), request.getPlayerId());
	}

	@MessageMapping("/create-room")
	public void createGame(CreateRoomRequest request) {
		this.gameRoomService.createGameRoom(request.getPlayerCount(), request.getHostId());
	}
}

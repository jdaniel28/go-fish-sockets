package com.danny.fish.service;

import com.danny.fish.socket.GameSocketHandler;
import com.danny.fish.constants.AppConstants;
import com.danny.fish.exception.PlayerNotFoundException;
import com.danny.fish.model.GameRoom;
import com.danny.fish.model.GameRoomView;
import com.danny.fish.model.Player;
import com.danny.fish.model.PlayerGameView;
import com.danny.fish.model.event.*;
import com.danny.fish.repo.GameRoomDao;
import com.danny.fish.repo.PlayerDao;
import com.danny.fish.util.CardActions;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Service
@Slf4j
public class GameRoomService {

	private final GameRoomDao gameRoomDao;

	private final PlayerDao playerDao;

	private final ApplicationEventPublisher eventPublisher;

//	private final SimpMessagingTemplate messagingTemplate;

	private final GameSocketHandler gameSocketHandler;

	@Autowired
	public GameRoomService(GameRoomDao gameRoomDao, ApplicationEventPublisher eventPublisher, PlayerDao playerDao, GameSocketHandler gameSocketHandler) {
		this.gameRoomDao = gameRoomDao;
		this.playerDao = playerDao;
		this.eventPublisher = eventPublisher;
//		this.messagingTemplate = messagingTemplate;
		this.gameSocketHandler = gameSocketHandler;
	}

//	@Transactional
	public GameRoom createGameRoom(int numPlayers, long hostId) {
		log.info("Creating a new game room with number of players {} with host {} ...", numPlayers, hostId);
		// Implementation for creating a game room
		GameRoom gameRoom = new GameRoom();
		Player player = playerDao.findById(hostId).orElseThrow(() -> new PlayerNotFoundException("Player with id "+hostId+" not found"));
		gameRoom.setTotalPlayerCount(numPlayers);
		gameRoom.setHostPlayerId(hostId);
		gameRoom.setJoinCode(CardActions.getAlphaNumericString(5));
		gameRoom.assignPlayer(hostId);
		gameRoom = this.gameRoomDao.save(gameRoom);
		log.info("Created a new game room with id {}", gameRoom.getId());
		this.eventPublisher.publishEvent(new RoomUpdatedEvent(this, gameRoom, "Game room created for " + numPlayers + " players by " + player.getPlayerName() + " with room code " + gameRoom.getJoinCode()));
		return gameRoom;
	}

	public GameRoom getGameRoom(long roomId) {
		log.info("Fetching game room with id {} ...", roomId);
		return this.gameRoomDao.findById(roomId).orElse(null);
	}

	@Transactional
	public boolean addPlayerToRoom(String roomCode, long playerId) {
		log.info("Adding player {} to game room {} ...", playerId, roomCode);
		boolean addedPlayer = false;
		GameRoom gameRoom = this.gameRoomDao.findByJoinCode(roomCode);
		boolean sendRoomUpdate = true;
		if (Objects.nonNull(gameRoom)) {
			gameRoom.assignPlayer(playerId);
			this.gameRoomDao.save(gameRoom);
			addedPlayer = true;
			log.info("Added player {} to game room {}", playerId, roomCode);
			if(gameRoom.getAssignedPlayersIds().size() == gameRoom.getTotalPlayerCount()){
				this.eventPublisher.publishEvent(new GameRoomReadyEvent(this, gameRoom.getId(), gameRoom.getAssignedPlayersIds().stream().map(GameRoom.AssignedPlayers::getPlayerId).toList(), gameRoom));;
				sendRoomUpdate = false;
			}
		}
		Player player = this.playerDao.findById(playerId).orElseThrow(() -> new PlayerNotFoundException("Player with id "+playerId+" not found"));
		if(sendRoomUpdate){
			this.eventPublisher.publishEvent(new RoomUpdatedEvent(this, gameRoom, "Player "+ player.getPlayerName() + " added to room " + roomCode + "!"));
		}
		return addedPlayer;
	}

	@EventListener
	public void startGame(GameStartedEvent gameStartedEvent) {
		log.info("Starting game in room {} ...", gameStartedEvent.getRoomId());
		GameRoom gameRoom = this.gameRoomDao.findById(gameStartedEvent.getRoomId()).orElse(null);
		if(Objects.nonNull(gameRoom)){
			gameRoom.setGameStarted(true);
			this.gameRoomDao.save(gameRoom);
			log.info("Game started in room {}", gameStartedEvent.getRoomId());
		}
	}

	public boolean removePlayerFromRoom(String roomId, long playerId) {
		log.info("Removing player {} from game room {} ...", playerId, roomId);
		boolean removedPlayer = false;
		GameRoom gameRoom = this.gameRoomDao.findByJoinCode(roomId);
		if (Objects.nonNull(gameRoom)) {
			removedPlayer = gameRoom.removePlayer(playerId);
			this.gameRoomDao.save(gameRoom);
			log.info("Removed player {} from game room {}", playerId, roomId);
		}
		Player player = this.playerDao.findById(playerId).orElseThrow(() -> new PlayerNotFoundException("Player with id "+playerId+" not found"));
		this.eventPublisher.publishEvent(new RoomUpdatedEvent(this, gameRoom, "Player "+ player.getPlayerName() + " removed from room " + roomId));
		return removedPlayer;
	}

	@EventListener
	public void handleRoomUpdatedEvent(RoomUpdatedEvent roomUpdatedEvent){
		GameRoomView gameRoomView = new GameRoomView();
		GameRoom gameRoom = roomUpdatedEvent.getGameRoom();
		if(Objects.nonNull(gameRoom)){
			gameRoomView.setRoomCode(gameRoom.getJoinCode());
			gameRoomView.setRoomId(gameRoom.getId());
			gameRoomView.setPlayerInfoMap(gameRoom.getAssignedPlayersIds().stream().map(GameRoom.AssignedPlayers::getPlayerId).map(pid -> {
				Player p = this.playerDao.findById(pid).orElse(null);
				if(Objects.nonNull(p)){
					PlayerGameView.PlayerInfo playerInfo = new PlayerGameView.PlayerInfo();
					playerInfo.setPlayerId(p.getPlayerId());
					playerInfo.setPlayerName(p.getPlayerName());
					playerInfo.setDeclaredRanks(Collections.emptyList());
					return playerInfo;
				}
				return null;
			}).filter(Objects::nonNull).collect(java.util.stream.Collectors.toMap((PlayerGameView.PlayerInfo::getPlayerId), pi -> pi)));
			gameRoomView.setTotalPlayers(gameRoom.getTotalPlayerCount());
			gameRoomView.setEventOccurred(roomUpdatedEvent.getRoomUpdatedEvent());
			String gameRoomViewJson = AppConstants.GSON.toJson(gameRoomView);
			gameRoom.getAssignedPlayersIds().parallelStream().forEach(playerId -> {
				try {
					this.gameSocketHandler.sendState(playerId.getPlayerId(), gameRoomViewJson);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	@EventListener
	public void handleJoinGameEvent(AddPlayerEvent addPlayerEvent){
		this.addPlayerToRoom(addPlayerEvent.getAddPlayerToRoomRequest().getRoomCode(), addPlayerEvent.getAddPlayerToRoomRequest().getPlayerId());
	}

	@EventListener
	public void handleCreateGameRoomEvent(CreateGameEvent createGameEvent){
		this.createGameRoom(createGameEvent.getCreateRoomRequest().getPlayerCount(), createGameEvent.getCreateRoomRequest().getHostId());
	}
}

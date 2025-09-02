package com.danny.fish.service;

import com.danny.fish.constants.AppConstants;
import com.danny.fish.constants.CardRank;
import com.danny.fish.constants.GameStatus;
import com.danny.fish.exception.GameNotFoundException;
import com.danny.fish.exception.PlayerNotFoundException;
import com.danny.fish.model.*;
import com.danny.fish.model.event.AskCardEvent;
import com.danny.fish.model.event.GameRoomReadyEvent;
import com.danny.fish.model.event.GameUpdatedEvent;
import com.danny.fish.repo.GameDao;
import com.danny.fish.repo.GameRoomDao;
import com.danny.fish.repo.PlayerDao;
import com.danny.fish.socket.GameSocketHandler;
import com.danny.fish.util.CardActions;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class GameService {

	private final GameDao gameDao;

	private final ApplicationEventPublisher eventPublisher;

	private final GameRoomDao gameRoomDao;

	private final PlayerDao playerDao;

	private final GameSocketHandler gameSocketHandler;

	@Autowired
	public GameService(GameDao gameDao, ApplicationEventPublisher eventPublisher, GameRoomDao gameRoomDao, PlayerDao playerDao, GameSocketHandler gameSocketHandler){
		this.gameDao = gameDao;
		this.gameRoomDao = gameRoomDao;
		this.eventPublisher = eventPublisher;
		this.playerDao = playerDao;
		this.gameSocketHandler = gameSocketHandler;
	}

	@EventListener(classes = {GameRoomReadyEvent.class})
	public void startGame(GameRoomReadyEvent gameRoomReadyEvent){
		log.info("Game room {} is ready to start the game ...", gameRoomReadyEvent.getRoomId());
		Game game = this.startGame(gameRoomReadyEvent.getGameRoom().getId(), gameRoomReadyEvent.getPlayerIds());
		this.pushUpdatesToPlayers(game);
	}

	private Game startGame(long roomId, List<Long> playerIds){
		log.debug("Starting game for room id {} with {} players ...", roomId, playerIds.size());
		List<Card> deckCards = new ArrayList<>(CardActions.DEFAULT_DECK);
		int playerNum = playerIds.size();
		Game game = new Game();
		game.setGameStatus(GameStatus.IN_PROGRESS);
		Collections.shuffle(deckCards);
		List<Game.GamePlayerHand> playerHands = new ArrayList<>();
		int cardMultiplier = 0;
		if (playerNum == 2 || playerNum == 3) {
			cardMultiplier = 7;
		} else {
			cardMultiplier = 5;
		}
		long playerTurnId = 1;
		for (int i = 0; i < playerNum; i++) {
			Long playerId = playerIds.get(i);
			Game.GamePlayerHand playerHand = new Game.GamePlayerHand();
			playerHand.setPlayerId(playerId);
			playerHand.setPlayerTurnId(playerTurnId);
			List<Card> playerHandCards = new ArrayList<>();
			for (int j = i * cardMultiplier; j < ((i + 1) * cardMultiplier); j++) {
				playerHandCards.add(deckCards.get(j));
			}
			playerHand.setHand(AppConstants.GSON.toJson(playerHandCards));
			playerHand.setGame(game);
			playerHands.add(playerHand);
			++playerTurnId;
		}
		game.setPlayerToPlayId(playerIds.get(0));
		game.setGameRoom(gameRoomDao.getReferenceById(roomId));
		game.setPlayerHands(playerHands);

		for (int i = 0; i < (cardMultiplier * playerNum); i++) {
			deckCards.removeFirst();
		}
		game.setDeck(AppConstants.GSON.toJson(deckCards));
		game = gameDao.save(game);
		return game;
	}

	public Game askCard(PlayerRequestCard requestCard) {
		log.info("Player {} is asking player {} for rank {} in game {} ...", requestCard.getRequestingPlayerId(), requestCard.getRequestedPlayerId(), requestCard.getAskedRank().getDisplay(), requestCard.getGameId());
		Game game = this.gameDao.findById(requestCard.getGameId()).orElseThrow(() -> new GameNotFoundException("Game with id " + requestCard.getGameId() + " not found"));
		CardRank askedRank = requestCard.getAskedRank();
		Game.GamePlayerHand requestedPlayerHand = game.getPlayerHands().stream().filter(playerHand -> playerHand.getPlayerId().equals(requestCard.getRequestedPlayerId())).findFirst().orElseThrow(() -> new PlayerNotFoundException("Player with id " + requestCard.getRequestedPlayerId() + " not found in game " + requestCard.getGameId()));
		Game.GamePlayerHand requestingPlayerHand = game.getPlayerHands().stream().filter(playerHand -> playerHand.getPlayerId().equals(requestCard.getRequestingPlayerId())).findFirst().orElseThrow(() -> new PlayerNotFoundException("Player with id " + requestCard.getRequestedPlayerId() + " not found in game " + requestCard.getGameId()));
		List<Card> requestedPlayerCards = AppConstants.GSON.fromJson(requestedPlayerHand.getHand(), new TypeToken<List<Card>>() {}.getType());
		List<Card> requestingPlayerCards = AppConstants.GSON.fromJson(requestingPlayerHand.getHand(), new TypeToken<List<Card>>() {}.getType());
		List<Card> deckCards = game.getDeck() != null ? AppConstants.GSON.fromJson(game.getDeck(), new TypeToken<List<Card>>() {}.getType()) : new ArrayList<>();

		boolean hasRank = false;
		StringBuilder logBuilder = new StringBuilder();
		Player askedPlayer = this.playerDao.findById(requestCard.getRequestedPlayerId()).orElse(null);
		Player askingPlayer = this.playerDao.findById(game.getPlayerToPlayId()).orElse(null);
		if(!CollectionUtils.isEmpty(requestedPlayerCards)){
			final boolean[] firstLog = {true};
			List<Card> matchingCards = requestedPlayerCards.stream().filter(card -> {
				if(firstLog[0]){
					logBuilder.append("Player ").append(askingPlayer.getPlayerName()).append(" took ");
					firstLog[0] = false;
				}else{
					logBuilder.append(", ");
				}
				logBuilder.append(card.getRank().getDisplay()).append(card.getSuit().getDisplay());
				return card.getRank().equals(askedRank);
			}).toList();
			if(!firstLog[0]){
				logBuilder.append(" from ").append(askedPlayer.getPlayerName());
			}
			hasRank = !CollectionUtils.isEmpty(matchingCards);
			if(hasRank){
				requestingPlayerCards.addAll(matchingCards);
				requestedPlayerCards.removeAll(matchingCards);
			}else{
				logBuilder.append("Player ").append(askingPlayer.getPlayerName()).append(" asked ").append(askedPlayer.getPlayerName()).append(" for ").append(askedRank.getDisplay());
				if(!CollectionUtils.isEmpty(deckCards)){
					logBuilder.append(" but went fishing instead 🐟");
					Card drawnCard = deckCards.getFirst();
					deckCards.removeFirst();
					requestingPlayerCards.add(drawnCard);
					game.setDeck(AppConstants.GSON.toJson(deckCards));
				}
			}
		}

		List<Game.GameLogEntry> gameLogEntries = game.getGameLogEntries();
		Game.GameLogEntry logEntry = new Game.GameLogEntry();
		logEntry.setLogEntry(logBuilder.toString());
		logEntry.setTimestampMillis(System.currentTimeMillis());
		gameLogEntries.add(logEntry);

		requestingPlayerHand.setHand(AppConstants.GSON.toJson(requestingPlayerCards));
		requestedPlayerHand.setHand(AppConstants.GSON.toJson(requestingPlayerHand));

		long currentPlayerTurnId = requestingPlayerHand.getPlayerTurnId();
		boolean nextPlayerFound = false;

		long nextPlayerTurnIdFinal = currentPlayerTurnId;

		while(!nextPlayerFound){
			long nextPlayerTurnId = game.getPlayerHands().size() == currentPlayerTurnId ? 1 : currentPlayerTurnId + 1;
			Game.GamePlayerHand nextPlayerHand = game.getPlayerHands().stream().filter(hand -> hand.getPlayerTurnId() == nextPlayerTurnId).findFirst().orElseThrow(() -> new PlayerNotFoundException("Player with turn id " + nextPlayerTurnId + " not found..."));
			List<Card> nextPlayerCards = nextPlayerHand.getHand() == null ? new ArrayList<>() : AppConstants.GSON.fromJson(nextPlayerHand.getHand(), new TypeToken<List<Card>>() {}.getType());
			nextPlayerFound = !CollectionUtils.isEmpty(nextPlayerCards);
			StringBuilder logBuilderTwo = new StringBuilder();
			if(!nextPlayerFound){
				logBuilderTwo.append("Skipping player ").append(this.playerDao.findById(nextPlayerHand.getPlayerId()).orElseThrow(() -> new PlayerNotFoundException("No player with id " + nextPlayerHand.getPlayerId() + " found...")).getPlayerName()).append(" as they have no cards left.");
				Game.GameLogEntry logEntryTwo = new Game.GameLogEntry();
				logEntryTwo.setLogEntry(logBuilderTwo.toString());
				logEntryTwo.setTimestampMillis(System.currentTimeMillis());
				gameLogEntries.add(logEntryTwo);
			}
			currentPlayerTurnId = nextPlayerTurnId;
			nextPlayerTurnIdFinal = nextPlayerTurnId;
		}
		game.setPlayerToPlayId(nextPlayerTurnIdFinal);
		game.setGameLogEntries(gameLogEntries);
		this.gameDao.save(game);
		return game;
	}

	private long getNextPlayerTurnId(Game game, long currentPlayerTurnId){
		boolean nextPlayerFound = false;
		long nextPlayerTurnIdFinal = currentPlayerTurnId;
		while(!nextPlayerFound){
			long nextPlayerTurnId = game.getPlayerHands().size() == currentPlayerTurnId ? 1 : currentPlayerTurnId + 1;
			Game.GamePlayerHand nextPlayerHand = game.getPlayerHands().stream().filter(hand -> hand.getPlayerTurnId() == nextPlayerTurnId).findFirst().orElseThrow(() -> new PlayerNotFoundException("Player with turn id " + nextPlayerTurnId + " not found..."));
			List<Card> nextPlayerCards = nextPlayerHand.getHand() == null ? new ArrayList<>() : AppConstants.GSON.fromJson(nextPlayerHand.getHand(), new TypeToken<List<Card>>() {}.getType());
			nextPlayerFound = !CollectionUtils.isEmpty(nextPlayerCards);
			currentPlayerTurnId = nextPlayerTurnId;
			nextPlayerTurnIdFinal = nextPlayerTurnId;
		}
		return nextPlayerTurnIdFinal;
	}

	private PlayerGameView constructGamePlayerView(Game game, long playerId){
		PlayerGameView playerGameView = new PlayerGameView();
		playerGameView.setPlayerId(playerId);
		playerGameView.setGameId(game.getId());

		game.getPlayerHands().stream().filter(playerHand -> playerHand.getPlayerId().equals(playerId)).findFirst().ifPresent(playerHand -> {
			List<Card> playerCards = AppConstants.GSON.fromJson(playerHand.getHand(), new TypeToken<List<Card>>() {}.getType());
			playerGameView.setPlayerCards(playerCards);
		});
		playerGameView.setGameLogEntries(game.getGameLogEntries());
		playerGameView.setCurrentPlayerTurnId(game.getPlayerToPlayId());
		return playerGameView;
	}

	public Game declareSet(PlayerDeclareRank declareRank){
		Game game = this.gameDao.findById(declareRank.getGameId()).orElseThrow(() -> new GameNotFoundException("Game with id " + declareRank.getGameId() + " not found"));
		if(game.getPlayerToPlayId() == declareRank.getPlayerId()){
			game.getPlayerHands().stream().filter(playerHand -> playerHand.getPlayerId().equals(declareRank.getPlayerId())).findFirst().ifPresent(playerHand -> {
				List<Card> playerCards = AppConstants.GSON.fromJson(playerHand.getHand(), new TypeToken<List<Card>>() {
				}.getType());
				List<Card> cardsOfRank = playerCards.stream().filter(card -> card.getRank().equals(declareRank.getDeclaredRank())).toList();
				if (cardsOfRank.size() == 4) {
					playerCards.removeAll(cardsOfRank);
					playerHand.setHand(AppConstants.GSON.toJson(playerCards));
					List<CardRank> declaredRanks = AppConstants.GSON.fromJson(playerHand.getDeclaredRanks(), new TypeToken<List<CardRank>>() {
					}.getType());
					if (declaredRanks == null) {
						declaredRanks = new ArrayList<>();
					}
					declaredRanks.add(declareRank.getDeclaredRank());
					playerHand.setDeclaredRanks(AppConstants.GSON.toJson(declaredRanks));
					String logStr = "Player " + this.playerDao.findById(declareRank.getPlayerId()).orElseThrow(() -> new PlayerNotFoundException("Player with id " + declareRank.getPlayerId() + " not found...")).getPlayerName() + " has declared they have collected all 4 " + declareRank.getDeclaredRank().getDisplay() + "'s.";
					Game.GameLogEntry logEntry = new Game.GameLogEntry();
					logEntry.setLogEntry(logStr);
					logEntry.setTimestampMillis(System.currentTimeMillis());
					game.getGameLogEntries().add(logEntry);

					if(playerCards.isEmpty()){
						List<Card> deckCards = game.getDeck() != null ? AppConstants.GSON.fromJson(game.getDeck(), new TypeToken<List<Card>>() {}.getType()) : new ArrayList<>();
						if(!CollectionUtils.isEmpty(deckCards)){
							Card card = deckCards.getFirst();
							playerCards.add(card);
							deckCards.removeFirst();
							game.setDeck(AppConstants.GSON.toJson(deckCards));
							playerHand.setHand(AppConstants.GSON.toJson(playerCards));
						}else{
							long nextPlayerTurnId = getNextPlayerTurnId(game, playerHand.getPlayerTurnId());
							game.setPlayerToPlayId(nextPlayerTurnId);
						}
					}
				}else{
					String logStr = "Player " + this.playerDao.findById(declareRank.getPlayerId()).orElseThrow(() -> new PlayerNotFoundException("Player with id " + declareRank.getPlayerId() + " not found...")).getPlayerName() + " attempted to declare " + declareRank.getDeclaredRank().getDisplay() + " but does not have all 4.";
					Game.GameLogEntry logEntry = new Game.GameLogEntry();
					logEntry.setLogEntry(logStr);
					logEntry.setTimestampMillis(System.currentTimeMillis());
					game.getGameLogEntries().add(logEntry);
				}
				this.gameDao.save(game);
			});
		}
		return game;
	}

	public void pushUpdatesToPlayers(Game game){
		game.getPlayerHands().parallelStream().forEach(gamePlayerHand -> {
			PlayerGameView playerGameView = this.constructGamePlayerView(game, gamePlayerHand.getPlayerId());
			this.eventPublisher.publishEvent(new GameUpdatedEvent(this, playerGameView));
		});
	}

	@EventListener
	public void handleGameUpdatedEvent(GameUpdatedEvent gameUpdatedEvent){
		log.debug("Pushing game update to player {} for game {} ...", gameUpdatedEvent.getPlayerGameView().getPlayerId(), gameUpdatedEvent.getPlayerGameView().getGameId());
		GameRoomView gameRoomView = this.constructGameRoomView(gameUpdatedEvent.getPlayerGameView(), gameUpdatedEvent.getEventOccurred());
		try {
			this.gameSocketHandler.sendState(gameRoomView.getPlayerGameView().getPlayerId(), AppConstants.GSON.toJson(gameRoomView));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	private GameRoomView constructGameRoomView(PlayerGameView playerGameView, String eventOccurred){
		GameRoomView gameRoomView = new GameRoomView();
		Game game = this.gameDao.findById(playerGameView.getGameId()).orElseThrow(() -> new GameNotFoundException("Game with id " + playerGameView.getGameId() + " not found"));
		GameRoom gameRoom = game.getGameRoom();
		gameRoomView.setRoomId(gameRoom.getId());
		gameRoomView.setRoomCode(gameRoom.getJoinCode());
		gameRoomView.setPlayerGameView(playerGameView);
		gameRoomView.setEventOccurred(eventOccurred);

		Map<Long, PlayerGameView.PlayerInfo> playerInfoMap = new HashMap<>();
		for(Game.GamePlayerHand playerHand : game.getPlayerHands()){
			Player player = this.playerDao.findById(playerHand.getPlayerId()).orElseThrow(() -> new PlayerNotFoundException("Player with id " + playerHand.getPlayerId() + " not found..."));
			PlayerGameView.PlayerInfo playerInfo = new PlayerGameView.PlayerInfo();
			playerInfo.setPlayerId(playerHand.getPlayerId());
			playerInfo.setPlayerName(player.getPlayerName());
			String declaredRanksStr = playerHand.getDeclaredRanks();
			List<CardRank> declaredRanks = AppConstants.GSON.fromJson(playerHand.getDeclaredRanks(), new TypeToken<List<CardRank>>() {}.getType());
			playerInfo.setDeclaredRanks(declaredRanks);
			playerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
		}
		gameRoomView.setPlayerInfoMap(playerInfoMap);
		gameRoomView.setTotalPlayers(gameRoom.getTotalPlayerCount());
		return gameRoomView;
	}

	@EventListener
	public void handleAskCardEvent(AskCardEvent askCardEvent){
		PlayerRequestCard requestCard = askCardEvent.getPlayerRequestCard();
		Game game = this.askCard(requestCard);
		this.pushUpdatesToPlayers(game);
	}

	public void handleDeclareSetEvent(PlayerDeclareRank declareRank) {
		Game game = this.declareSet(declareRank);
		this.pushUpdatesToPlayers(game);
	}

}

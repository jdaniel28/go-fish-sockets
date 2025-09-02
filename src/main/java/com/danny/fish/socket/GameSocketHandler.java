package com.danny.fish.socket;

import com.danny.fish.constants.AppConstants;
import com.danny.fish.model.request.AddPlayerToRoomRequest;
import com.danny.fish.model.request.CreateRoomRequest;
import com.danny.fish.model.PlayerDeclareRank;
import com.danny.fish.model.PlayerRequestCard;
import com.danny.fish.model.event.AddPlayerEvent;
import com.danny.fish.model.event.AskCardEvent;
import com.danny.fish.model.event.CreateGameEvent;
import com.danny.fish.model.event.DeclareSetEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

	private final ApplicationEventPublisher eventPublisher;

	public GameSocketHandler(ApplicationEventPublisher eventPublisher){
		this.eventPublisher = eventPublisher;
	}

	private final String PLAYER_ID_HEADER = "pId";
	private final String ROOM_CODE_HEADER = "rCode";

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	private String key(Long playerId) {
		return String.valueOf(playerId);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
//		String roomCode = session.getHandshakeHeaders().get(ROOM_CODE_HEADER).getFirst();
		Long playerId = Long.valueOf(session.getHandshakeHeaders().get(PLAYER_ID_HEADER).getFirst());
		sessions.put(key(playerId), session);
		session.getAttributes().put(PLAYER_ID_HEADER, playerId);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//		String roomCode = session.getHandshakeHeaders().get(ROOM_CODE_HEADER).getFirst();
		Long playerId = Long.valueOf(session.getHandshakeHeaders().get(PLAYER_ID_HEADER).getFirst());
		sessions.remove(key(playerId));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload(); // <-- the raw string data sent by client
		System.out.println("Message from " + session.getId() + ": " + payload);
//		addToSessions(message, session);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode json = mapper.readTree(payload);
		String type = json.get("type").asText();
		String requestData = json.get("data").textValue();

		Long playerId = (Long) session.getAttributes().get(PLAYER_ID_HEADER);
		switch (type) {
			case "CREATE_GAME":
				CreateRoomRequest createRoomRequest = AppConstants.GSON.fromJson(requestData, CreateRoomRequest.class);
				createRoomRequest.setHostId(playerId);
				this.eventPublisher.publishEvent(new CreateGameEvent(this, createRoomRequest));
				break;
			case "JOIN_GAME":
				AddPlayerToRoomRequest addPlayerToRoomRequest = AppConstants.GSON.fromJson(requestData, AddPlayerToRoomRequest.class);
				addPlayerToRoomRequest.setPlayerId(playerId);
				this.eventPublisher.publishEvent(new AddPlayerEvent(this, addPlayerToRoomRequest));
				break;
			case "ASK_CARD":
				PlayerRequestCard playerRequestCard = AppConstants.GSON.fromJson(requestData, PlayerRequestCard.class);
				playerRequestCard.setRequestedPlayerId(playerId);
				this.eventPublisher.publishEvent(new AskCardEvent(this, playerRequestCard));
				break;
			case "DECLARE_SET":
				PlayerDeclareRank playerDeclareRank = AppConstants.GSON.fromJson(requestData, PlayerDeclareRank.class);
				playerDeclareRank.setPlayerId(playerId);
				this.eventPublisher.publishEvent(new DeclareSetEvent(this, playerDeclareRank));
				break;
			default:
				System.out.println("Unknown message type: " + type);
		}

		// Optionally parse JSON
		// GameMessage msg = new ObjectMapper().readValue(payload, GameMessage.class);
	}


	public void sendState(Long playerId, String jsonState) throws Exception {
		WebSocketSession session = sessions.get(key(playerId));
		if (session != null && session.isOpen()) {
			session.sendMessage(new TextMessage(jsonState));
		}
	}

	private void addToSessions(TextMessage message, WebSocketSession session){
		String[] headerBody = parseTextMessage(message.getPayload());
		Map<String, String> headers = getHeaders(headerBody[0]);
		sessions.put(key(Long.valueOf(headers.get(PLAYER_ID_HEADER))), session);
	}

	private String[] parseTextMessage(String payload) {
		String HEADER_BODY_DELIMITER = "---";
		return payload.split(HEADER_BODY_DELIMITER);
	}

	private Map<String, String> getHeaders(String headerStr){
		Map<String, String> headers = new HashMap<>();
		String[] headerLines = headerStr.split("\n");
		for (String line : headerLines) {
			String[] parts = line.split(":");
			if (parts.length == 2) {
				headers.put(parts[0].trim(), parts[1].trim());
			}
		}
		return headers;
	}
}


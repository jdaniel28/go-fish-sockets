package com.danny.fish.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	private final GameSocketHandler gameWebSocketHandler;

	public WebSocketConfig(GameSocketHandler gameWebSocketHandler) {
		this.gameWebSocketHandler = gameWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(gameWebSocketHandler, "/ws/game")
				.addInterceptors(new HttpSessionHandshakeInterceptor()) // optional
				.setAllowedOrigins("*"); // or restrict domains
	}

}

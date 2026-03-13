package com.kitten.kitten_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configure le serveur WebSocket avec le protocole STOMP
 *
 * Les clients se connectent sur /ws, envoient des messages préfixés par /app
 * et s'abonnent à des topics sous /topic (broadcast) ou /queue (personnel)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// broker in-memory pour distribuer les messages aux abonnés
		config.enableSimpleBroker("/topic", "/queue");
		// préfixe pour les messages destinés aux @MessageMapping
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// point d'entrée WebSocket, ouvert à tous les origines pour le dev
		registry.addEndpoint("/ws").setAllowedOrigins("*");
	}
}

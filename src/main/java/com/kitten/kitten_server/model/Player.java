package com.kitten.kitten_server.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un joueur connecté à une room
 * L'id est généré automatiquement et ne change jamais
 */
@Getter
public class Player {

	/** Identifiant unique stable du joueur */
	private final String id;

	/** Nom affiché dans la room */
	@Setter private String username;

	/** Id de session WebSocket, change si le joueur se reconnecte */
	@Setter private String sessionId;

	/** Indique si ce joueur est l'hôte de la room */
	@Setter private boolean host;

	public Player(String username, String sessionId) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
		this.sessionId = sessionId;
		this.host = false;
	}
}

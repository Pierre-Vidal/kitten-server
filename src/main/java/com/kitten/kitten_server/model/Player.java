package com.kitten.kitten_server.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {

	private final String id;
	@Setter private String username;
	@Setter private String sessionId;
	@Setter private boolean host;

	public Player(String username, String sessionId) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
		this.sessionId = sessionId;
		this.host = false;
	}
}

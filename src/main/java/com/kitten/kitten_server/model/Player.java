package com.kitten.kitten_server.model;

import java.util.UUID;

public class Player {

	private final String id;
	private String username;
	private String sessionId;
	private boolean host;

	public Player(String username, String sessionId) {
		this.id = UUID.randomUUID().toString();
		this.username = username;
		this.sessionId = sessionId;
		this.host = false;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isHost() {
		return host;
	}

	public void setHost(boolean host) {
		this.host = host;
	}
}

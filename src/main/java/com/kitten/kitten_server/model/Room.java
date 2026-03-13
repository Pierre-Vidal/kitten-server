package com.kitten.kitten_server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

@Getter
public class Room {

	private final String code;
	private final Map<String, Player> players;
	private String hostId;
	private final Map<String, Object> state;

	public Room(String code) {
		this.code = code;
		this.players = new ConcurrentHashMap<>();
		this.state = new ConcurrentHashMap<>();
	}

	public void addPlayer(Player player) {
		if (players.isEmpty()) {
			player.setHost(true);
			this.hostId = player.getSessionId();
		}
		players.put(player.getSessionId(), player);
	}

	public void removePlayer(String sessionId) {
		Player removed = players.remove(sessionId);
		if (removed != null && removed.isHost() && !players.isEmpty()) {
			Player newHost = players.values().iterator().next();
			newHost.setHost(true);
			this.hostId = newHost.getSessionId();
		}
	}

	public void updateState(Map<String, Object> newState) {
		state.putAll(newState);
	}

	public int getPlayerCount() {
		return players.size();
	}
}

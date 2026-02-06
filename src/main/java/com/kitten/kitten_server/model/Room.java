package com.kitten.kitten_server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	public int getPlayerCount() {
		return players.size();
	}

	public String getCode() {
		return code;
	}

	public Map<String, Player> getPlayers() {
		return players;
	}

	public String getHostId() {
		return hostId;
	}

	public Map<String, Object> getState() {
		return state;
	}
}

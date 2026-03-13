package com.kitten.kitten_server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

/**
 * Représente une room de jeu
 * Gère la liste des joueurs, l'hôte courant et l'état partagé du jeu
 */
@Getter
public class Room {

	/** Code court à 6 lettres pour rejoindre la room */
	private final String code;

	/** Joueurs indexés par leur sessionId pour un accès rapide */
	private final Map<String, Player> players;

	/** SessionId du joueur qui est actuellement hôte */
	private String hostId;

	/** État partagé du jeu, libre format (scores, round, etc.) */
	private final Map<String, Object> state;

	public Room(String code) {
		this.code = code;
		this.players = new ConcurrentHashMap<>();
		this.state = new ConcurrentHashMap<>();
	}

	/**
	 * Ajoute un joueur dans la room
	 * Le premier joueur ajouté devient automatiquement l'hôte
	 */
	public void addPlayer(Player player) {
		if (players.isEmpty()) {
			player.setHost(true);
			this.hostId = player.getSessionId();
		}
		players.put(player.getSessionId(), player);
	}

	/**
	 * Retire un joueur de la room
	 * Si c'était l'hôte, le prochain joueur dans la map prend le relais
	 */
	public void removePlayer(String sessionId) {
		Player removed = players.remove(sessionId);
		if (removed != null && removed.isHost() && !players.isEmpty()) {
			Player newHost = players.values().iterator().next();
			newHost.setHost(true);
			this.hostId = newHost.getSessionId();
		}
	}

	/**
	 * Merge les nouvelles valeurs dans l'état existant
	 * Les clés déjà présentes sont écrasées, les autres conservées
	 */
	public void updateState(Map<String, Object> newState) {
		state.putAll(newState);
	}

	public int getPlayerCount() {
		return players.size();
	}
}

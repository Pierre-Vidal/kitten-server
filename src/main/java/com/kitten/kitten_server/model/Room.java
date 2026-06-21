package com.kitten.kitten_server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kitten.kitten_server.exception.RoomFullException;
import lombok.Getter;

/**
 * Représente une room de jeu
 * Gère la liste des joueurs, l'hôte courant et l'état partagé du jeu
 *
 * [C2.2.1] Modèle central du prototype — encapsule les règles métier de la room
 * [C2.2.3] ConcurrentHashMap pour la thread-safety en environnement WebSocket multi-thread
 */
@Getter
public class Room {

	private static final int DEFAULT_MAX_PLAYERS = 8;

	/** Code court à 6 lettres pour rejoindre la room */
	private final String code;

	/** Joueurs indexés par leur sessionId pour un accès rapide */
	private final Map<String, Player> players;

	/** SessionId du joueur qui est actuellement hôte */
	private String hostId;

	/** État partagé du jeu, libre format (scores, round, etc.) */
	private final Map<String, Object> state;

	/** Nombre max de joueurs autorisés dans la room */
	private final int maxPlayers;

	/** Statut de la room : WAITING ou IN_GAME */
	private RoomStatus status;

	/** Si true, les nouveaux joueurs peuvent rejoindre même si IN_GAME */
	private final boolean allowJoinInGame;

	public Room(String code) {
		this(code, DEFAULT_MAX_PLAYERS, false);
	}

	public Room(String code, int maxPlayers, boolean allowJoinInGame) {
		this.code = code;
		this.maxPlayers = maxPlayers;
		this.allowJoinInGame = allowJoinInGame;
		this.players = new ConcurrentHashMap<>();
		this.state = new ConcurrentHashMap<>();
		this.status = RoomStatus.WAITING;
	}

	/**
	 * Ajoute un joueur dans la room
	 * Le premier joueur ajouté devient automatiquement l'hôte
	 * @throws RoomFullException si la room est pleine
	 */
	public void addPlayer(Player player) {
		if (players.size() >= maxPlayers) {
			throw new RoomFullException(maxPlayers);
		}
		if (players.isEmpty()) {
			player.setHost(true);
			this.hostId = player.getSessionId();
		}
		players.put(player.getSessionId(), player);
	}

	/**
	 * Retire un joueur de la room
	 * Si c'était l'hôte, le prochain joueur dans la map prend le relais
	 *
	 * [C2.3.2] Transfert automatique de host — correction d'un cas limite identifié en recette
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

	public void clearState() {
		state.clear();
	}

	public void setStatus(RoomStatus status) {
		this.status = status;
	}

	public int getPlayerCount() {
		return players.size();
	}
}

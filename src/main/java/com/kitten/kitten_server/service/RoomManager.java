package com.kitten.kitten_server.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;

/**
 * Gère le cycle de vie des rooms : création, rejoindre, quitter
 * Maintient aussi un index session → room pour retrouver rapidement la room d'un joueur
 */
@Service
public class RoomManager {

	private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int CODE_LENGTH = 6;
	private static final SecureRandom RANDOM = new SecureRandom();

	/** Toutes les rooms actives, indexées par leur code */
	private final Map<String, Room> rooms = new ConcurrentHashMap<>();

	/** Permet de retrouver la room d'un joueur depuis son sessionId */
	private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

	/**
	 * Crée une nouvelle room avec un code unique et y ajoute le host
	 */
	public Room createRoom(Player host) {
		String code = generateCode();
		Room room = new Room(code);
		room.addPlayer(host);
		rooms.put(code, room);
		sessionToRoom.put(host.getSessionId(), code);
		return room;
	}

	/**
	 * Fait rejoindre un joueur dans une room existante
	 * @throws IllegalArgumentException si le code ne correspond à aucune room
	 */
	public Room joinRoom(String code, Player player) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + code);
		}
		room.addPlayer(player);
		sessionToRoom.put(player.getSessionId(), code);
		return room;
	}

	/**
	 * Retire un joueur de la room
	 * Si la room devient vide, elle est supprimée et null est retourné
	 * @throws IllegalArgumentException si le code ne correspond à aucune room
	 */
	public Room leaveRoom(String code, String sessionId) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + code);
		}
		room.removePlayer(sessionId);
		sessionToRoom.remove(sessionId);

		// room vide : on la supprime
		if (room.getPlayerCount() == 0) {
			rooms.remove(code);
			return null;
		}
		return room;
	}

	public Room getRoom(String code) {
		return rooms.get(code);
	}

	public String getRoomCodeBySession(String sessionId) {
		return sessionToRoom.get(sessionId);
	}

	public Map<String, Room> getRooms() {
		return rooms;
	}

	/**
	 * Génère un code aléatoire à 6 lettres majuscules
	 * Boucle jusqu'à trouver un code pas encore utilisé
	 */
	private String generateCode() {
		String code;
		do {
			StringBuilder sb = new StringBuilder(CODE_LENGTH);
			for (int i = 0; i < CODE_LENGTH; i++) {
				sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
			}
			code = sb.toString();
		} while (rooms.containsKey(code));
		return code;
	}
}

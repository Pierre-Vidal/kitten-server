package com.kitten.kitten_server.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kitten.kitten_server.exception.AlreadyInRoomException;
import com.kitten.kitten_server.exception.NotInRoomException;
import com.kitten.kitten_server.exception.RoomFullException;
import com.kitten.kitten_server.exception.RoomNotFoundException;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;

/**
 * Gère le cycle de vie des rooms : création, rejoindre, quitter
 * Maintient aussi un index session → room pour retrouver rapidement la room d'un joueur
 *
 * [C2.2.1] Service métier central du prototype Kitten
 * [C2.2.3] Limite à 8 joueurs par room, codes uniques générés par SecureRandom
 * [C2.3.1] Chaque méthode lève une exception typée testable en recette (RoomNotFoundException, etc.)
 */
@Service
public class RoomManager {

	private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int CODE_LENGTH = 6;
	private static final int MAX_PLAYERS = 8;
	private static final SecureRandom RANDOM = new SecureRandom();

	/** Toutes les rooms actives, indexées par leur code */
	private final Map<String, Room> rooms = new ConcurrentHashMap<>();

	/** Permet de retrouver la room d'un joueur depuis son sessionId */
	private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

	/**
	 * Crée une nouvelle room avec un code unique et y ajoute le host
	 * @throws AlreadyInRoomException si le joueur est déjà dans une room
	 */
	public Room createRoom(Player host) {
		if (sessionToRoom.containsKey(host.getSessionId())) {
			throw new AlreadyInRoomException();
		}
		String code = generateCode();
		Room room = new Room(code);
		room.addPlayer(host);
		rooms.put(code, room);
		sessionToRoom.put(host.getSessionId(), code);
		return room;
	}

	/**
	 * Fait rejoindre un joueur dans une room existante
	 * @throws RoomNotFoundException si le code ne correspond à aucune room
	 * @throws AlreadyInRoomException si le joueur est déjà dans une room
	 * @throws RoomFullException si la room a atteint le maximum de joueurs
	 */
	public Room joinRoom(String code, Player player) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new RoomNotFoundException(code);
		}
		if (sessionToRoom.containsKey(player.getSessionId())) {
			throw new AlreadyInRoomException();
		}
		if (room.getPlayerCount() >= MAX_PLAYERS) {
			throw new RoomFullException(code);
		}
		room.addPlayer(player);
		sessionToRoom.put(player.getSessionId(), code);
		return room;
	}

	/**
	 * Retire un joueur de la room
	 * Si la room devient vide, elle est supprimée et null est retourné
	 * @throws NotInRoomException si le joueur n'est dans aucune room
	 * @throws RoomNotFoundException si le code ne correspond à aucune room
	 */
	public Room leaveRoom(String code, String sessionId) {
		String actualCode = sessionToRoom.get(sessionId);
		if (actualCode == null || !actualCode.equals(code)) {
			throw new NotInRoomException();
		}
		Room room = rooms.get(code);
		if (room == null) {
			throw new RoomNotFoundException(code);
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

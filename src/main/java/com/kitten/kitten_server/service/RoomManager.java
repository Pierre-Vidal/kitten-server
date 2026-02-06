package com.kitten.kitten_server.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;

@Service
public class RoomManager {

	private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int CODE_LENGTH = 6;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final Map<String, Room> rooms = new ConcurrentHashMap<>();

	public Room createRoom(Player host) {
		String code = generateCode();
		Room room = new Room(code);
		room.addPlayer(host);
		rooms.put(code, room);
		return room;
	}

	public Room joinRoom(String code, Player player) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + code);
		}
		room.addPlayer(player);
		return room;
	}

	public Room leaveRoom(String code, String sessionId) {
		Room room = rooms.get(code);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + code);
		}
		room.removePlayer(sessionId);
		if (room.getPlayerCount() == 0) {
			rooms.remove(code);
			return null;
		}
		return room;
	}

	public Room getRoom(String code) {
		return rooms.get(code);
	}

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

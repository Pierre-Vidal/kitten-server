package com.kitten.kitten_server.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import com.kitten.kitten_server.dto.CreateRoomRequest;
import com.kitten.kitten_server.dto.EventType;
import com.kitten.kitten_server.dto.JoinRoomRequest;
import com.kitten.kitten_server.dto.LeaveRoomRequest;
import com.kitten.kitten_server.dto.RoomEvent;
import com.kitten.kitten_server.dto.RoomResponse;
import com.kitten.kitten_server.dto.StateUpdateRequest;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.service.RoomManager;

import lombok.RequiredArgsConstructor;

/**
 * Contrôleur WebSocket qui gère les actions sur les rooms
 * Chaque méthode correspond à un message STOMP envoyé par le client
 *
 * [C2.2.1] Couche contrôleur du prototype — point d'entrée de toutes les actions client
 * [C2.2.3] @Validated + @Valid sur chaque méthode pour rejeter les inputs invalides avant
 *           d'atteindre la logique métier
 * [C2.3.1] 4 actions testables en recette : create, join, leave, state
 */
@Controller
@Validated
@RequiredArgsConstructor
public class RoomController {

	private final RoomManager roomManager;
	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * Crée une room et notifie le créateur via sa queue personnelle
	 * On envoie aussi un broadcast à la room pour que les autres soient au courant
	 */
	@MessageMapping("/room/create")
	public void createRoom(@Valid CreateRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player host = new Player(request.getUsername(), sessionId);
		Room room = roomManager.createRoom(host);

		RoomEvent event = new RoomEvent(EventType.ROOM_CREATED, toResponse(room), toPlayerInfo(host));
		sendToSession(sessionId, "/queue/room", event);
		broadcastToRoom(room.getCode(), event);
	}

	/**
	 * Fait rejoindre un joueur et broadcast l'info à toute la room
	 */
	@MessageMapping("/room/join")
	public void joinRoom(@Valid JoinRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player player = new Player(request.getUsername(), sessionId);
		Room room = roomManager.joinRoom(request.getCode(), player);

		broadcastToRoom(room.getCode(), new RoomEvent(EventType.PLAYER_JOINED, toResponse(room), toPlayerInfo(player)));
	}

	/**
	 * Retire un joueur de la room
	 * Si l'hôte est parti, un second event HOST_CHANGED est broadcasté
	 */
	@MessageMapping("/room/leave")
	public void leaveRoom(@Valid LeaveRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Room currentRoom = roomManager.getRoom(request.getCode());
		String oldHostId = currentRoom != null ? currentRoom.getHostId() : null;
		Room room = roomManager.leaveRoom(request.getCode(), sessionId);

		if (room != null) {
			RoomResponse response = toResponse(room);
			broadcastToRoom(room.getCode(), new RoomEvent(EventType.PLAYER_LEFT, response, sessionId));

			if (oldHostId != null && !oldHostId.equals(room.getHostId())) {
				broadcastToRoom(room.getCode(), new RoomEvent(EventType.HOST_CHANGED, response, room.getHostId()));
			}
		}
	}

	/**
	 * Met à jour l'état de la room et le broadcast à tous les joueurs
	 * Ne fait rien si la room n'existe plus
	 */
	@MessageMapping("/room/state")
	public void updateState(@Valid StateUpdateRequest request, SimpMessageHeaderAccessor headerAccessor) {
		Room room = roomManager.getRoom(request.getCode());
		if (room == null) {
			return;
		}
		room.updateState(request.getState());
		broadcastToRoom(room.getCode(), new RoomEvent(EventType.STATE_UPDATED, toResponse(room), request.getState()));
	}

	/** Envoie un message à tous les joueurs de la room */
	private void broadcastToRoom(String roomCode, RoomEvent event) {
		messagingTemplate.convertAndSend("/topic/room/" + roomCode, event);
	}

	/**
	 * Envoie un message directement à une session spécifique
	 * On doit forcer le sessionId dans les headers car il n'y a pas d'authentification
	 */
	private void sendToSession(String sessionId, String destination, Object payload) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		accessor.setSessionId(sessionId);
		messagingTemplate.convertAndSendToUser(sessionId, destination, payload, accessor.getMessageHeaders());
	}

	private RoomResponse toResponse(Room room) {
		List<RoomResponse.PlayerInfo> players = room.getPlayers().values().stream()
				.map(this::toPlayerInfo)
				.toList();
		return new RoomResponse(room.getCode(), players, room.getHostId(), room.getState());
	}

	private RoomResponse.PlayerInfo toPlayerInfo(Player player) {
		return new RoomResponse.PlayerInfo(player.getId(), player.getUsername(), player.isHost());
	}
}

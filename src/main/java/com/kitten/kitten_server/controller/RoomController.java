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

@Controller
@Validated
@RequiredArgsConstructor
public class RoomController {

	private final RoomManager roomManager;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/room/create")
	public void createRoom(@Valid CreateRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player host = new Player(request.getUsername(), sessionId);
		Room room = roomManager.createRoom(host);

		RoomEvent event = new RoomEvent(EventType.ROOM_CREATED, toResponse(room), toPlayerInfo(host));
		sendToSession(sessionId, "/queue/room", event);
		broadcastToRoom(room.getCode(), event);
	}

	@MessageMapping("/room/join")
	public void joinRoom(@Valid JoinRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player player = new Player(request.getUsername(), sessionId);
		Room room = roomManager.joinRoom(request.getCode(), player);

		broadcastToRoom(room.getCode(), new RoomEvent(EventType.PLAYER_JOINED, toResponse(room), toPlayerInfo(player)));
	}

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

	@MessageMapping("/room/state")
	public void updateState(@Valid StateUpdateRequest request, SimpMessageHeaderAccessor headerAccessor) {
		Room room = roomManager.getRoom(request.getCode());
		if (room == null) {
			return;
		}
		room.updateState(request.getState());
		broadcastToRoom(room.getCode(), new RoomEvent(EventType.STATE_UPDATED, toResponse(room), request.getState()));
	}

	private void broadcastToRoom(String roomCode, RoomEvent event) {
		messagingTemplate.convertAndSend("/topic/room/" + roomCode, event);
	}

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

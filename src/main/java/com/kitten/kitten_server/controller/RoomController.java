package com.kitten.kitten_server.controller;

import java.util.List;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kitten.kitten_server.dto.CreateRoomRequest;
import com.kitten.kitten_server.dto.JoinRoomRequest;
import com.kitten.kitten_server.dto.RoomResponse;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.service.RoomManager;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomController {

	private final RoomManager roomManager;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/room/create")
	public void createRoom(CreateRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player host = new Player(request.getUsername(), sessionId);
		Room room = roomManager.createRoom(host);

		RoomResponse response = toResponse(room);
		messagingTemplate.convertAndSendToUser(sessionId, "/queue/room", response, createHeaders(sessionId));
		messagingTemplate.convertAndSend("/topic/room/" + room.getCode(), response);
	}

	@MessageMapping("/room/join")
	public void joinRoom(JoinRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Player player = new Player(request.getUsername(), sessionId);
		Room room = roomManager.joinRoom(request.getCode(), player);

		messagingTemplate.convertAndSend("/topic/room/" + room.getCode(), toResponse(room));
	}

	@MessageMapping("/room/leave")
	public void leaveRoom(JoinRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
		String sessionId = headerAccessor.getSessionId();
		Room room = roomManager.leaveRoom(request.getCode(), sessionId);

		if (room != null) {
			messagingTemplate.convertAndSend("/topic/room/" + room.getCode(), toResponse(room));
		}
	}

	private MessageHeaders createHeaders(String sessionId) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		accessor.setSessionId(sessionId);
		return accessor.getMessageHeaders();
	}

	private RoomResponse toResponse(Room room) {
		List<RoomResponse.PlayerInfo> players = room.getPlayers().values().stream()
				.map(p -> new RoomResponse.PlayerInfo(p.getId(), p.getUsername(), p.isHost()))
				.toList();
		return new RoomResponse(room.getCode(), players, room.getHostId());
	}
}

package com.kitten.kitten_server.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.kitten.kitten_server.dto.EventType;
import com.kitten.kitten_server.dto.RoomEvent;
import com.kitten.kitten_server.dto.RoomResponse;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.service.RoomManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final RoomManager roomManager;
	private final SimpMessagingTemplate messagingTemplate;

	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		String roomCode = roomManager.getRoomCodeBySession(sessionId);

		if (roomCode == null) {
			return;
		}

		String oldHostId = roomManager.getRoom(roomCode).getHostId();
		log.info("Joueur deconnecte (session: {}), retrait de la room {}", sessionId, roomCode);
		Room room = roomManager.leaveRoom(roomCode, sessionId);

		if (room != null) {
			RoomResponse response = toResponse(room);
			messagingTemplate.convertAndSend("/topic/room/" + roomCode,
					new RoomEvent(EventType.PLAYER_LEFT, response, sessionId));

			if (!oldHostId.equals(room.getHostId())) {
				messagingTemplate.convertAndSend("/topic/room/" + roomCode,
						new RoomEvent(EventType.HOST_CHANGED, response, room.getHostId()));
			}
		}
	}

	private RoomResponse toResponse(Room room) {
		List<RoomResponse.PlayerInfo> players = room.getPlayers().values().stream()
				.map(p -> new RoomResponse.PlayerInfo(p.getId(), p.getUsername(), p.isHost()))
				.toList();
		return new RoomResponse(room.getCode(), players, room.getHostId(), room.getState());
	}
}

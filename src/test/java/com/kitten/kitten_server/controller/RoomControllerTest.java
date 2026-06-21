package com.kitten.kitten_server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.kitten.kitten_server.dto.ChangeStatusRequest;
import com.kitten.kitten_server.dto.CreateRoomRequest;
import com.kitten.kitten_server.dto.EventType;
import com.kitten.kitten_server.dto.JoinRoomRequest;
import com.kitten.kitten_server.dto.LeaveRoomRequest;
import com.kitten.kitten_server.dto.RoomEvent;
import com.kitten.kitten_server.dto.StateUpdateRequest;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.model.RoomStatus;
import com.kitten.kitten_server.service.RoomManager;

// [C2.2.2] Tests unitaires du contrôleur avec Mockito — vérifient les broadcasts et events sans démarrer le serveur
// [C2.1.1] Utilisation de Mockito pour isoler les dépendances et tester en environnement contrôlé
// [C2.3.1] 9 scénarios de recette couvrant create, join, leave (avec/sans transfert host), state, erreur
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomControllerTest {

	@Mock
	private RoomManager roomManager;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private SimpMessageHeaderAccessor headerAccessor;

	@InjectMocks
	private RoomController roomController;

	private Room room;
	private Player host;

	@BeforeEach
	void setUp() {
		room = new Room("ABCDEF");
		host = new Player("Alice", "session-1");
		room.addPlayer(host);

		when(headerAccessor.getSessionId()).thenReturn("session-1");
	}

	@Test
	void createRoomEnvoieEventAuHostEtBroadcast() {
		when(roomManager.createRoom(any(Player.class), any(), any(Boolean.class))).thenReturn(room);

		CreateRoomRequest request = new CreateRoomRequest();
		request.setUsername("Alice");
		roomController.createRoom(request, headerAccessor);

		// Envoi perso au host
		verify(messagingTemplate).convertAndSendToUser(eq("session-1"), eq("/queue/room"), any(RoomEvent.class), any(Map.class));
		// Broadcast à la room
		verify(messagingTemplate).convertAndSend(eq("/topic/room/ABCDEF"), any(RoomEvent.class));
	}

	@Test
	void createRoomEnvoieEventDeTypeROOM_CREATED() {
		when(roomManager.createRoom(any(Player.class), any(), any(Boolean.class))).thenReturn(room);

		CreateRoomRequest request = new CreateRoomRequest();
		request.setUsername("Alice");
		roomController.createRoom(request, headerAccessor);

		verify(messagingTemplate).convertAndSend(
				eq("/topic/room/ABCDEF"),
				argMatchingType(EventType.ROOM_CREATED));
	}

	@Test
	void joinRoomBroadcastePLAYER_JOINED() {
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(bob);
		when(roomManager.joinRoom(eq("ABCDEF"), any(Player.class))).thenReturn(room);

		JoinRoomRequest request = new JoinRoomRequest();
		request.setCode("ABCDEF");
		request.setUsername("Bob");
		roomController.joinRoom(request, headerAccessor);

		verify(messagingTemplate).convertAndSend(
				eq("/topic/room/ABCDEF"),
				argMatchingType(EventType.PLAYER_JOINED));
	}

	@Test
	void leaveRoomBroadcastePLAYER_LEFT() {
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(bob);
		when(headerAccessor.getSessionId()).thenReturn("session-2");
		when(roomManager.getRoom("ABCDEF")).thenReturn(room);
		when(roomManager.leaveRoom("ABCDEF", "session-2")).thenAnswer(inv -> {
			room.removePlayer("session-2");
			return room;
		});

		LeaveRoomRequest request = new LeaveRoomRequest();
		request.setCode("ABCDEF");
		roomController.leaveRoom(request, headerAccessor);

		verify(messagingTemplate).convertAndSend(
				eq("/topic/room/ABCDEF"),
				argMatchingType(EventType.PLAYER_LEFT));
	}

	@Test
	void leaveRoomBroadcasteHOST_CHANGEDSiHostPart() {
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(bob);
		when(roomManager.getRoom("ABCDEF")).thenReturn(room);
		when(roomManager.leaveRoom("ABCDEF", "session-1")).thenAnswer(inv -> {
			room.removePlayer("session-1");
			return room;
		});

		LeaveRoomRequest request = new LeaveRoomRequest();
		request.setCode("ABCDEF");
		roomController.leaveRoom(request, headerAccessor);

		verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/room/ABCDEF"), any(RoomEvent.class));
	}

	@Test
	void leaveRoomPasDHOST_CHANGEDSiNonHostPart() {
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(bob);
		when(headerAccessor.getSessionId()).thenReturn("session-2");
		when(roomManager.getRoom("ABCDEF")).thenReturn(room);
		when(roomManager.leaveRoom("ABCDEF", "session-2")).thenAnswer(inv -> {
			room.removePlayer("session-2");
			return room;
		});

		LeaveRoomRequest request = new LeaveRoomRequest();
		request.setCode("ABCDEF");
		roomController.leaveRoom(request, headerAccessor);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/ABCDEF"), any(RoomEvent.class));
	}

	@Test
	void leaveRoomRienSiRoomVide() {
		when(roomManager.getRoom("ABCDEF")).thenReturn(room);
		when(roomManager.leaveRoom("ABCDEF", "session-1")).thenReturn(null);

		LeaveRoomRequest request = new LeaveRoomRequest();
		request.setCode("ABCDEF");
		roomController.leaveRoom(request, headerAccessor);

		verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
	}

	@Test
	void updateStateBroadcasteSTATE_UPDATED() {
		when(roomManager.getRoom("ABCDEF")).thenReturn(room);

		StateUpdateRequest request = new StateUpdateRequest();
		request.setCode("ABCDEF");
		request.setState(Map.of("score", 10));
		roomController.updateState(request, headerAccessor);

		verify(messagingTemplate).convertAndSend(
				eq("/topic/room/ABCDEF"),
				argMatchingType(EventType.STATE_UPDATED));
	}

	@Test
	void updateStateRienSiRoomInexistante() {
		when(roomManager.getRoom("XXXXXX")).thenReturn(null);

		StateUpdateRequest request = new StateUpdateRequest();
		request.setCode("XXXXXX");
		request.setState(Map.of("score", 10));
		roomController.updateState(request, headerAccessor);

		verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
	}

	@Test
	void changeStatusBroadcasteSTATUS_CHANGED() {
		when(roomManager.changeStatus("ABCDEF", "session-1", RoomStatus.IN_GAME)).thenReturn(room);

		ChangeStatusRequest request = new ChangeStatusRequest();
		request.setCode("ABCDEF");
		request.setStatus(RoomStatus.IN_GAME);
		roomController.changeStatus(request, headerAccessor);

		verify(messagingTemplate).convertAndSend(
				eq("/topic/room/ABCDEF"),
				argMatchingType(EventType.STATUS_CHANGED));
	}

	private RoomEvent argMatchingType(EventType expectedType) {
		return org.mockito.ArgumentMatchers.argThat(event -> {
			if (!(event instanceof RoomEvent)) return false;
			return ((RoomEvent) event).getType() == expectedType;
		});
	}
}

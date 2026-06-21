package com.kitten.kitten_server.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.kitten.kitten_server.dto.RoomEvent;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.service.RoomManager;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private RoomManager roomManager;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketEventListener listener;

    private Room room;
    private Player host;

    @BeforeEach
    void setUp() {
        room = new Room("ABCDEF");
        host = new Player("Alice", "session-1");
        room.addPlayer(host);
    }

    private SessionDisconnectEvent buildEvent(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
                org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = (Message<byte[]>) org.springframework.messaging.support.MessageBuilder
                .withPayload(new byte[0])
                .setHeaders(accessor)
                .build();
        return new SessionDisconnectEvent(new Object(), message, sessionId, CloseStatus.NORMAL);
    }

    @Test
    void deconnexionJoueurPasEnRoomNeFaitRien() {
        when(roomManager.getRoomCodeBySession("session-X")).thenReturn(null);

        listener.handleDisconnect(buildEvent("session-X"));

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void deconnexionJoueurBroadcastePLAYER_LEFT() {
        Player bob = new Player("Bob", "session-2");
        room.addPlayer(bob);
        when(roomManager.getRoomCodeBySession("session-2")).thenReturn("ABCDEF");
        when(roomManager.getRoom("ABCDEF")).thenReturn(room);
        when(roomManager.leaveRoom("ABCDEF", "session-2")).thenAnswer(inv -> {
            room.removePlayer("session-2");
            return room;
        });

        listener.handleDisconnect(buildEvent("session-2"));

        verify(messagingTemplate).convertAndSend(eq("/topic/room/ABCDEF"), any(RoomEvent.class));
    }

    @Test
    void deconnexionHostBroadcasteHOST_CHANGED() {
        Player bob = new Player("Bob", "session-2");
        room.addPlayer(bob);
        when(roomManager.getRoomCodeBySession("session-1")).thenReturn("ABCDEF");
        when(roomManager.getRoom("ABCDEF")).thenReturn(room);
        when(roomManager.leaveRoom("ABCDEF", "session-1")).thenAnswer(inv -> {
            room.removePlayer("session-1");
            return room;
        });

        listener.handleDisconnect(buildEvent("session-1"));

        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/room/ABCDEF"), any(RoomEvent.class));
    }

    @Test
    void deconnexionDernierJoueurSupprimeRoom() {
        when(roomManager.getRoomCodeBySession("session-1")).thenReturn("ABCDEF");
        when(roomManager.getRoom("ABCDEF")).thenReturn(room);
        when(roomManager.leaveRoom("ABCDEF", "session-1")).thenReturn(null);

        listener.handleDisconnect(buildEvent("session-1"));

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }
}

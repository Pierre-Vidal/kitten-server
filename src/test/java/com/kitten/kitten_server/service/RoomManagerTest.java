package com.kitten.kitten_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;

// [C2.2.2] Tests unitaires du service RoomManager — couverture des cas nominaux et d'erreur
// [C2.3.1] Scénarios de recette : création, jointure, départ, unicité des codes, gestion d'exceptions
class RoomManagerTest {

	private RoomManager roomManager;

	@BeforeEach
	void setUp() {
		roomManager = new RoomManager();
	}

	@Test
	void createRoomRetourneRoomAvecCode() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);

		assertThat(room).isNotNull();
		assertThat(room.getCode()).hasSize(6);
		assertThat(room.getCode()).matches("[A-Z]{6}");
	}

	@Test
	void createRoomAjouteLeHostCorrectement() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);

		assertThat(room.getPlayers()).hasSize(1);
		assertThat(room.getHostId()).isEqualTo("session-1");
	}

	@Test
	void joinRoomAjouteLeJoueur() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);

		Player bob = new Player("Bob", "session-2");
		Room joined = roomManager.joinRoom(room.getCode(), bob);

		assertThat(joined.getPlayers()).hasSize(2);
	}

	@Test
	void joinRoomInexistanteLanceException() {
		Player player = new Player("Bob", "session-2");

		assertThatThrownBy(() -> roomManager.joinRoom("XXXXXX", player))
				.isInstanceOf(com.kitten.kitten_server.exception.RoomNotFoundException.class);
	}

	@Test
	void leaveRoomRetireLeJoueur() {
		Player host = new Player("Alice", "session-1");
		Player bob = new Player("Bob", "session-2");
		Room room = roomManager.createRoom(host);
		roomManager.joinRoom(room.getCode(), bob);

		Room result = roomManager.leaveRoom(room.getCode(), "session-2");

		assertThat(result.getPlayers()).hasSize(1);
	}

	@Test
	void leaveRoomRetourneNullSiVide() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);

		Room result = roomManager.leaveRoom(room.getCode(), "session-1");

		assertThat(result).isNull();
		assertThat(roomManager.getRoom(room.getCode())).isNull();
	}

	@Test
	void getRoomCodeBySessionFonctionne() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);

		assertThat(roomManager.getRoomCodeBySession("session-1")).isEqualTo(room.getCode());
	}

	@Test
	void getRoomCodeBySessionNulleApresLeave() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host);
		roomManager.leaveRoom(room.getCode(), "session-1");

		assertThat(roomManager.getRoomCodeBySession("session-1")).isNull();
	}

	@Test
	void codesRoomsUniques() {
		for (int i = 0; i < 10; i++) {
			roomManager.createRoom(new Player("Player" + i, "session-" + i));
		}
		long distinct = roomManager.getRooms().values().stream()
				.map(Room::getCode)
				.distinct()
				.count();
		assertThat(distinct).isEqualTo(10);
	}
}

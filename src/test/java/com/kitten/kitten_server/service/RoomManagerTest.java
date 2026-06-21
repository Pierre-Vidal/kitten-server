package com.kitten.kitten_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kitten.kitten_server.exception.AlreadyInRoomException;
import com.kitten.kitten_server.exception.NotHostException;
import com.kitten.kitten_server.exception.NotInRoomException;
import com.kitten.kitten_server.exception.RoomInGameException;
import com.kitten.kitten_server.exception.RoomNotFoundException;
import com.kitten.kitten_server.model.Player;
import com.kitten.kitten_server.model.Room;
import com.kitten.kitten_server.model.RoomStatus;

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
		Room room = roomManager.createRoom(host, null, false);

		assertThat(room).isNotNull();
		assertThat(room.getCode()).hasSize(6);
		assertThat(room.getCode()).matches("[A-Z]{6}");
	}

	@Test
	void createRoomAjouteLeHostCorrectement() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		assertThat(room.getPlayers()).hasSize(1);
		assertThat(room.getHostId()).isEqualTo("session-1");
	}

	@Test
	void joinRoomAjouteLeJoueur() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

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
		Room room = roomManager.createRoom(host, null, false);
		roomManager.joinRoom(room.getCode(), bob);

		Room result = roomManager.leaveRoom(room.getCode(), "session-2");

		assertThat(result.getPlayers()).hasSize(1);
	}

	@Test
	void leaveRoomRetourneNullSiVide() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		Room result = roomManager.leaveRoom(room.getCode(), "session-1");

		assertThat(result).isNull();
		assertThat(roomManager.getRoom(room.getCode())).isNull();
	}

	@Test
	void getRoomCodeBySessionFonctionne() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		assertThat(roomManager.getRoomCodeBySession("session-1")).isEqualTo(room.getCode());
	}

	@Test
	void getRoomCodeBySessionNulleApresLeave() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);
		roomManager.leaveRoom(room.getCode(), "session-1");

		assertThat(roomManager.getRoomCodeBySession("session-1")).isNull();
	}

	@Test
	void codesRoomsUniques() {
		for (int i = 0; i < 10; i++) {
			roomManager.createRoom(new Player("Player" + i, "session-" + i), null, false);
		}
		long distinct = roomManager.getRooms().values().stream()
				.map(Room::getCode)
				.distinct()
				.count();
		assertThat(distinct).isEqualTo(10);
	}

	@Test
	void roomStatusWaitingParDefaut() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		assertThat(room.getStatus()).isEqualTo(RoomStatus.WAITING);
	}

	@Test
	void changeStatusPasseEnInGame() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		roomManager.changeStatus(room.getCode(), "session-1", RoomStatus.IN_GAME);

		assertThat(room.getStatus()).isEqualTo(RoomStatus.IN_GAME);
	}

	@Test
	void changeStatusRefuseSiPasHost() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		assertThatThrownBy(() -> roomManager.changeStatus(room.getCode(), "session-2", RoomStatus.IN_GAME))
				.isInstanceOf(NotHostException.class);
	}

	@Test
	void joinBloqueeSiInGameEtAllowJoinInGameFalse() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);
		roomManager.changeStatus(room.getCode(), "session-1", RoomStatus.IN_GAME);
		Player bob = new Player("Bob", "session-2");
		String code = room.getCode();

		assertThatThrownBy(() -> roomManager.joinRoom(code, bob))
				.isInstanceOf(RoomInGameException.class);
	}

	@Test
	void joinAutoriseeSiInGameEtAllowJoinInGameTrue() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, true);
		roomManager.changeStatus(room.getCode(), "session-1", RoomStatus.IN_GAME);
		Player bob = new Player("Bob", "session-2");
		String code = room.getCode();

		Room result = roomManager.joinRoom(code, bob);

		assertThat(result.getPlayers()).hasSize(2);
	}

	@Test
	void allowJoinInGameFalseParDefaut() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);

		assertThat(room.isAllowJoinInGame()).isFalse();
	}

	@Test
	void allowJoinInGameTrueSiConfigureALaCreation() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, true);

		assertThat(room.isAllowJoinInGame()).isTrue();
	}

	@Test
	void createRoomLanceExceptionSiDejaEnRoom() {
		Player host = new Player("Alice", "session-1");
		roomManager.createRoom(host, null, false);

		assertThatThrownBy(() -> roomManager.createRoom(host, null, false))
				.isInstanceOf(AlreadyInRoomException.class);
	}

	@Test
	void joinRoomLanceExceptionSiDejaEnRoom() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);
		String code = room.getCode();

		assertThatThrownBy(() -> roomManager.joinRoom(code, host))
				.isInstanceOf(AlreadyInRoomException.class);
	}

	@Test
	void leaveRoomLanceExceptionSiPasDansLaRoom() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, null, false);
		String code = room.getCode();

		assertThatThrownBy(() -> roomManager.leaveRoom(code, "session-inconnu"))
				.isInstanceOf(NotInRoomException.class);
	}

	@Test
	void leaveRoomLanceExceptionSiMauvaisCode() {
		Player host = new Player("Alice", "session-1");
		roomManager.createRoom(host, null, false);

		assertThatThrownBy(() -> roomManager.leaveRoom("XXXXXX", "session-1"))
				.isInstanceOf(NotInRoomException.class);
	}

	@Test
	void leaveRoomLanceExceptionSiCodeNeConcordePas() {
		Player host1 = new Player("Alice", "session-1");
		Player host2 = new Player("Bob", "session-2");
		roomManager.createRoom(host1, null, false);
		Room room2 = roomManager.createRoom(host2, null, false);
		String autreCode = room2.getCode();
		String sessionId = "session-1";

		assertThatThrownBy(() -> roomManager.leaveRoom(autreCode, sessionId))
				.isInstanceOf(NotInRoomException.class);
	}

	@Test
	void changeStatusLanceExceptionSiRoomInexistante() {
		assertThatThrownBy(() -> roomManager.changeStatus("XXXXXX", "session-1", RoomStatus.IN_GAME))
				.isInstanceOf(RoomNotFoundException.class);
	}

	@Test
	void createRoomAvecMaxPlayersPersonnalise() {
		Player host = new Player("Alice", "session-1");
		Room room = roomManager.createRoom(host, 4, false);

		assertThat(room.getMaxPlayers()).isEqualTo(4);
	}
}

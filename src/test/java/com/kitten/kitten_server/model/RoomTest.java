package com.kitten.kitten_server.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kitten.kitten_server.exception.RoomFullException;

// [C2.2.2] Tests unitaires du modèle Room — vérifient les règles métier isolément
// [C2.3.1] Scénarios de recette : attribution du host, transfert, comptage, état partagé
class RoomTest {

	private Room room;

	@BeforeEach
	void setUp() {
		room = new Room("ABCDEF");
	}

	@Test
	void premierJoueurDevientHost() {
		Player player = new Player("Alice", "session-1");
		room.addPlayer(player);

		assertThat(player.isHost()).isTrue();
		assertThat(room.getHostId()).isEqualTo("session-1");
	}

	@Test
	void deuxiemeJoueurNestPasHost() {
		room.addPlayer(new Player("Alice", "session-1"));
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(bob);

		assertThat(bob.isHost()).isFalse();
	}

	@Test
	void transfertHostQuandHostQuitte() {
		Player alice = new Player("Alice", "session-1");
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(alice);
		room.addPlayer(bob);

		room.removePlayer("session-1");

		assertThat(bob.isHost()).isTrue();
		assertThat(room.getHostId()).isEqualTo("session-2");
	}

	@Test
	void pasDeTransfertSiNonHostQuitte() {
		Player alice = new Player("Alice", "session-1");
		Player bob = new Player("Bob", "session-2");
		room.addPlayer(alice);
		room.addPlayer(bob);

		room.removePlayer("session-2");

		assertThat(alice.isHost()).isTrue();
		assertThat(room.getHostId()).isEqualTo("session-1");
	}

	@Test
	void getPlayerCountCorrect() {
		room.addPlayer(new Player("Alice", "s1"));
		room.addPlayer(new Player("Bob", "s2"));

		assertThat(room.getPlayerCount()).isEqualTo(2);
	}

	@Test
	void updateStateMergeLesDonnees() {
		room.updateState(Map.of("score", 10, "round", 1));
		room.updateState(Map.of("score", 20));

		assertThat(room.getState()).containsEntry("score", 20);
		assertThat(room.getState()).containsEntry("round", 1);
	}

	@Test
	void roomCodeCorrect() {
		assertThat(room.getCode()).isEqualTo("ABCDEF");
	}

	@Test
	void addPlayerLanceExceptionSiRoomPleine() {
		Room roomPleine = new Room("XYZABC", 2, false);
		roomPleine.addPlayer(new Player("Alice", "s1"));
		roomPleine.addPlayer(new Player("Bob", "s2"));
		Player charlie = new Player("Charlie", "s3");

		assertThatThrownBy(() -> roomPleine.addPlayer(charlie))
				.isInstanceOf(RoomFullException.class);
	}

	@Test
	void removePlayerSansEffetSiSessionInconnue() {
		room.addPlayer(new Player("Alice", "session-1"));

		room.removePlayer("session-inexistante");

		assertThat(room.getPlayerCount()).isEqualTo(1);
		assertThat(room.getHostId()).isEqualTo("session-1");
	}
}

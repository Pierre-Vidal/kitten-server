package com.kitten.kitten_server.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

		assertThat(room.getState().get("score")).isEqualTo(20);
		assertThat(room.getState().get("round")).isEqualTo(1);
	}

	@Test
	void roomCodeCorrect() {
		assertThat(room.getCode()).isEqualTo("ABCDEF");
	}
}

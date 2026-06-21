package com.kitten.kitten_server.dto;

import java.util.List;
import java.util.Map;

import com.kitten.kitten_server.model.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomResponse {
	private String code;
	private List<PlayerInfo> players;
	private String hostId;
	private Map<String, Object> state;
	private int maxPlayers;
	private RoomStatus status;
	private boolean allowJoinInGame;

	@Data
	@AllArgsConstructor
	public static class PlayerInfo {
		private String id;
		private String username;
		private boolean host;
	}
}

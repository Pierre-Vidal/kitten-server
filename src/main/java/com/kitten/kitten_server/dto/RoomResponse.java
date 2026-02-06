package com.kitten.kitten_server.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomResponse {
	private String code;
	private List<PlayerInfo> players;
	private String hostId;

	@Data
	@AllArgsConstructor
	public static class PlayerInfo {
		private String id;
		private String username;
		private boolean host;
	}
}

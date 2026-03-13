package com.kitten.kitten_server.dto;

import lombok.Data;

@Data
public class JoinRoomRequest {
	private String code;
	private String username;
}

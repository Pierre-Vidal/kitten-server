package com.kitten.kitten_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomEvent {
	private EventType type;
	private RoomResponse room;
	private Object data;
}

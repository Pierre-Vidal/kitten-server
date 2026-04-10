package com.kitten.kitten_server.exception;

public class RoomFullException extends KittenException {

	public RoomFullException(String code) {
		super(ErrorCode.ROOM_FULL, "La room " + code + " est pleine");
	}
}

package com.kitten.kitten_server.exception;

public class RoomNotFoundException extends KittenException {

	public RoomNotFoundException(String code) {
		super(ErrorCode.ROOM_NOT_FOUND, "Room introuvable : " + code);
	}
}

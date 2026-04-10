package com.kitten.kitten_server.exception;

public class NotInRoomException extends KittenException {

	public NotInRoomException() {
		super(ErrorCode.NOT_IN_ROOM, "Tu n'es dans aucune room");
	}
}

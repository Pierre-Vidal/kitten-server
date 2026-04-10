package com.kitten.kitten_server.exception;

public class AlreadyInRoomException extends KittenException {

	public AlreadyInRoomException() {
		super(ErrorCode.ALREADY_IN_ROOM, "Tu es déjà dans une room");
	}
}

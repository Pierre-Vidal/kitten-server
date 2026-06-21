package com.kitten.kitten_server.exception;

public class RoomInGameException extends KittenException {

    public RoomInGameException() {
        super(ErrorCode.ROOM_IN_GAME, "La partie est déjà en cours, impossible de rejoindre");
    }
}

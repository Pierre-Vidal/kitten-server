package com.kitten.kitten_server.exception;

public class NotHostException extends KittenException {

    public NotHostException() {
        super(ErrorCode.NOT_HOST, "Seul l'hôte peut changer le statut de la room");
    }
}

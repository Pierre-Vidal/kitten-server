package com.kitten.kitten_server.exception;

public class PlayerNotFoundException extends KittenException {

    public PlayerNotFoundException(String sessionId) {
        super(ErrorCode.PLAYER_NOT_FOUND, "Joueur introuvable : " + sessionId);
    }
}

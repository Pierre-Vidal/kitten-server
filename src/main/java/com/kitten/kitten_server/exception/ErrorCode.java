package com.kitten.kitten_server.exception;

/**
 * Codes d'erreur renvoyés au client dans un ErrorEvent
 * Le SDK peut les utiliser pour afficher le bon message ou déclencher la bonne action
 */
public enum ErrorCode {
	ROOM_NOT_FOUND,
	ROOM_FULL,
	VALIDATION_ERROR,
	ALREADY_IN_ROOM,
	NOT_IN_ROOM,
	ROOM_IN_GAME,
	NOT_HOST,
	PLAYER_NOT_FOUND,
	INTERNAL_ERROR
}

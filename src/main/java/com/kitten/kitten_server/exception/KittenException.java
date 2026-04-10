package com.kitten.kitten_server.exception;

import lombok.Getter;

/**
 * Exception de base pour toutes les erreurs métier de Kitten
 * Porte un ErrorCode pour que le client sache exactement ce qui s'est passé
 */
@Getter
public class KittenException extends RuntimeException {

	private final ErrorCode errorCode;

	public KittenException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}

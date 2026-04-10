package com.kitten.kitten_server.exception;

import lombok.Getter;

/**
 * Exception de base pour toutes les erreurs métier de Kitten
 * Porte un ErrorCode pour que le client sache exactement ce qui s'est passé
 *
 * [C2.2.3] Hiérarchie d'exceptions custom — toutes les erreurs métier étendent cette classe
 * [C2.3.2] ErrorCode machine-readable pour permettre une correction ciblée côté SDK
 */
@Getter
public class KittenException extends RuntimeException {

	private final ErrorCode errorCode;

	public KittenException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}

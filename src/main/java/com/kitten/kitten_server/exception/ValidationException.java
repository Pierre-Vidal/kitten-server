package com.kitten.kitten_server.exception;

public class ValidationException extends KittenException {

	public ValidationException(String message) {
		super(ErrorCode.VALIDATION_ERROR, message);
	}
}

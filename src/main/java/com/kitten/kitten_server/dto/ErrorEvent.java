package com.kitten.kitten_server.dto;

import com.kitten.kitten_server.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Objet envoyé au client quand quelque chose tourne mal
 * Le type est toujours ERROR, le code permet au SDK de réagir précisément
 */
@Data
@AllArgsConstructor
public class ErrorEvent {

	/** Toujours ERROR, pour que le SDK identifie le type de message */
	private final EventType type = EventType.ERROR;

	/** Code machine pour le SDK */
	private ErrorCode code;

	/** Message lisible pour le debug */
	private String message;
}

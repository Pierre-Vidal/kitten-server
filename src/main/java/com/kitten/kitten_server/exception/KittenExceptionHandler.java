package com.kitten.kitten_server.exception;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.kitten.kitten_server.dto.ErrorEvent;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepte toutes les exceptions levées dans les contrôleurs WebSocket
 * Renvoie un ErrorEvent propre à la session concernée au lieu de laisser planter silencieusement
 *
 * [C2.2.3] Gestion d'erreurs centralisée — un seul endroit pour tous les cas d'erreur
 * [C2.3.1] Chaque ErrorCode correspond à un scénario de test du cahier de recettes
 * [C2.3.2] Plan de correction : VALIDATION_ERROR → input client, ROOM_NOT_FOUND → logique métier,
 *           INTERNAL_ERROR → bug serveur à investiguer dans les logs
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class KittenExceptionHandler {

	private final SimpMessagingTemplate messagingTemplate;

	/** Erreurs métier Kitten : room introuvable, validation échouée, etc. */
	@MessageExceptionHandler(KittenException.class)
	public void handleKittenException(KittenException ex, SimpMessageHeaderAccessor headerAccessor) {
		log.warn("Erreur metier [{}] : {}", ex.getErrorCode(), ex.getMessage());
		sendError(headerAccessor.getSessionId(), ex.getErrorCode(), ex.getMessage());
	}

	/** Erreurs de validation sur les @MessageMapping (levée par @Valid sur les DTOs) */
	@MessageExceptionHandler(MethodArgumentNotValidException.class)
	public void handleMethodArgNotValid(MethodArgumentNotValidException ex, SimpMessageHeaderAccessor headerAccessor) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + " : " + f.getDefaultMessage())
				.findFirst()
				.orElse("Requête invalide");
		log.warn("Validation échouée : {}", message);
		sendError(headerAccessor.getSessionId(), ErrorCode.VALIDATION_ERROR, message);
	}

	/** Erreurs de validation Jakarta (@NotBlank, @Size, etc.) */
	@MessageExceptionHandler(ConstraintViolationException.class)
	public void handleValidation(ConstraintViolationException ex, SimpMessageHeaderAccessor headerAccessor) {
		String message = ex.getConstraintViolations().stream()
				.map(v -> v.getPropertyPath() + " : " + v.getMessage())
				.findFirst()
				.orElse("Requête invalide");
		log.warn("Validation échouée : {}", message);
		sendError(headerAccessor.getSessionId(), ErrorCode.VALIDATION_ERROR, message);
	}

	/** Filet de sécurité pour toute exception inattendue */
	@MessageExceptionHandler(Exception.class)
	public void handleGeneric(Exception ex, SimpMessageHeaderAccessor headerAccessor) {
		log.error("Erreur inattendue", ex);
		sendError(headerAccessor.getSessionId(), ErrorCode.INTERNAL_ERROR, "Une erreur interne s'est produite");
	}

	private void sendError(String sessionId, ErrorCode code, String message) {
		SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		accessor.setSessionId(sessionId);
		messagingTemplate.convertAndSendToUser(
				sessionId, "/queue/errors",
				new ErrorEvent(code, message),
				accessor.getMessageHeaders());
	}
}

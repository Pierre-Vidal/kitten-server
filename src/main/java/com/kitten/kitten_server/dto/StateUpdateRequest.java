package com.kitten.kitten_server.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// [C2.2.3] Validation que l'état envoyé est non-null — le state null provoquerait un comportement indéfini
// [C2.3.1] Scénario de recette : envoi d'un state null → VALIDATION_ERROR attendu
@Data
public class StateUpdateRequest {

	@NotBlank(message = "Le code de la room est obligatoire")
	private String code;

	@NotNull(message = "L'état ne peut pas être null")
	private Map<String, Object> state;
}

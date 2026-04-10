package com.kitten.kitten_server.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StateUpdateRequest {

	@NotBlank(message = "Le code de la room est obligatoire")
	private String code;

	@NotNull(message = "L'état ne peut pas être null")
	private Map<String, Object> state;
}

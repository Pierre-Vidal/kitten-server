package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoomRequest {

	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 2, max = 20, message = "Le nom doit faire entre 2 et 20 caractères")
	private String username;
}

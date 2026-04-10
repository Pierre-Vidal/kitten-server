package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// [C2.2.3] Validation stricte du code room (exactement 6 caractères) pour éviter les requêtes malformées
// [C2.3.1] Scénario de recette : tentative de jointure avec code trop court/long → VALIDATION_ERROR
@Data
public class JoinRoomRequest {

	@NotBlank(message = "Le code de la room est obligatoire")
	@Size(min = 6, max = 6, message = "Le code doit faire exactement 6 caractères")
	private String code;

	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 2, max = 20, message = "Le nom doit faire entre 2 et 20 caractères")
	private String username;
}

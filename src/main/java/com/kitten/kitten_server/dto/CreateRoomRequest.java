package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// [C2.2.3] Validation des inputs à la frontière du système — rejet des données invalides avant traitement
// [C2.3.1] Règles métier testables : username obligatoire, longueur entre 2 et 20 caractères
@Data
public class CreateRoomRequest {

	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 2, max = 20, message = "Le nom doit faire entre 2 et 20 caractères")
	private String username;
}

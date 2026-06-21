package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

	/** Nombre max de joueurs — optionnel, 8 par défaut, entre 2 et 50 */
	@Min(value = 2, message = "La room doit accepter au moins 2 joueurs")
	@Max(value = 50, message = "La room ne peut pas dépasser 50 joueurs")
	private Integer maxPlayers;

	/** Si true, les joueurs peuvent rejoindre même quand la room est IN_GAME — false par défaut */
	private boolean allowJoinInGame = false;
}

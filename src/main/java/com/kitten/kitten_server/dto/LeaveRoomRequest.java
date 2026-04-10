package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeaveRoomRequest {

	@NotBlank(message = "Le code de la room est obligatoire")
	@Size(min = 6, max = 6, message = "Le code doit faire exactement 6 caractères")
	private String code;
}

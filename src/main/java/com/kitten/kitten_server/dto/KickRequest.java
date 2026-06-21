package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KickRequest {

    @NotBlank(message = "Le code de la room est obligatoire")
    private String code;

    @NotBlank(message = "L'id du joueur à kicker est obligatoire")
    private String targetSessionId;
}

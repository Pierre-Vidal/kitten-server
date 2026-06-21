package com.kitten.kitten_server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetStateRequest {

    @NotBlank(message = "Le code de la room est obligatoire")
    private String code;
}

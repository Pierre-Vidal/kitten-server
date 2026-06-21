package com.kitten.kitten_server.dto;

import com.kitten.kitten_server.model.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {

    @NotBlank(message = "Le code de la room est obligatoire")
    private String code;

    @NotNull(message = "Le statut est obligatoire")
    private RoomStatus status;
}

package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartExecutionRequest {

    @NotNull(message = "Latitude do GPS é obrigatória")
    private Double gpsLatitude;

    @NotNull(message = "Longitude do GPS é obrigatória")
    private Double gpsLongitude;

    // Data/hora capturada no dispositivo do técnico (opcional — usa now() se nulo)
    private String startedAt;
}

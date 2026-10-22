package com.experiencefieldops.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteExecutionRequest {

    private Double endGpsLatitude;

    private Double endGpsLongitude;

    // Data/hora capturada no dispositivo do técnico (opcional — usa now() se nulo)
    private String completedAt;

    // Observações gerais do técnico ao finalizar
    private String notes;
}

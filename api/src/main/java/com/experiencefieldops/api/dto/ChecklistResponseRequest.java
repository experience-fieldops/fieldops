package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChecklistResponseRequest {

    @NotNull(message = "ID do item do snapshot é obrigatório")
    private Long snapshotItemId;

    // Valor da resposta como string (pode ser nulo quando notApplicable = true)
    private String value;

    // Observação do técnico
    private String observation;

    // Se true, o item é marcado como não aplicável e value é ignorado
    private Boolean notApplicable;

    // GPS no momento da resposta
    private Double gpsLatitude;

    private Double gpsLongitude;

    // Timestamp capturado no dispositivo (opcional — usa now() se nulo)
    private String respondedAt;
}

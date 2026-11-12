package com.experiencefieldops.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseRequest {

    @NotNull(message = "ID do item do snapshot é obrigatório")
    private Long snapshotItemId;

    /**
     * Valor da resposta como string.
     * Pode ser nulo quando notApplicable = true.
     */
    @Schema(description = "Valor da resposta, conforme o responseType do item. "
            + "CONFORMITY: CONFORMING | ATTENTION | NON_CONFORMING | NOT_VERIFIED. "
            + "BOOLEAN: true | false. NUMBER: decimal. DATE: yyyy-MM-dd. "
            + "Ignorado quando notApplicable = true.",
            example = "CONFORMING")
    private String value;

    // Observação do técnico (obrigatória em não conformidade quando configurado — RN-038)
    private String observation;

    // Se true, o item é marcado como não aplicável e value é ignorado
    private Boolean notApplicable;
}

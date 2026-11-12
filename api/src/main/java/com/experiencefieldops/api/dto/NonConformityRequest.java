package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.Severity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonConformityRequest {

    // Opcional quando snapshotItemId é informado: usa o título do item (B-10)
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String title;

    // Obrigatório quando severity = CRITICAL (validado no service — RN-055)
    private String description;

    @NotNull(message = "Severidade é obrigatória")
    private Severity severity;

    // ID do item do snapshot ao qual a NC está associada (opcional — RN-053)
    private Long snapshotItemId;

    // Ignorado: o backend calcula a partir das evidências vinculadas à NC (B-10). Mantido só por compatibilidade.
    @Deprecated
    private Boolean evidenceAttached;
}

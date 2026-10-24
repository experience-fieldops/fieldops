package com.experiencefieldops.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload do endpoint POST /api/v1/sync.
 *
 * Recebe um lote de operações coletadas offline pelo app mobile.
 * As operações são processadas na ordem de dependência definida por
 * SyncOperationType.ordinal() — START_INSPECTION antes de SAVE_RESPONSE,
 * SAVE_RESPONSE antes de COMPLETE_INSPECTION, etc.
 */
@Getter
@Setter
public class SyncRequest {

    @NotEmpty(message = "O lote deve conter ao menos uma operação")
    @Valid
    private List<SyncItemRequest> operations;
}

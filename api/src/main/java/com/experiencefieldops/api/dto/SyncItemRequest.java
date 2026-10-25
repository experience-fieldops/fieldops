package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.SyncOperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa uma operação individual dentro do payload de POST /api/v1/sync.
 *
 * Formatos de payload por operationType:
 *
 * START_INSPECTION:
 *   { "startedAtDevice": "2026-10-22T08:00:00" }
 *
 * SAVE_RESPONSE:
 *   { "snapshotItemId": 1, "value": "true", "observation": null, "notApplicable": false }
 *
 * SAVE_NON_CONFORMITY:
 *   { "title": "...", "description": "...", "severity": "HIGH",
 *     "snapshotItemId": 1, "evidenceAttached": false }
 *
 * UPLOAD_EVIDENCE:
 *   { "responseId": 1, "nonConformityId": null, "fileKey": "uuid.jpg" }
 *   (o upload real do arquivo ocorre via POST /api/v1/inspections/{id}/evidences)
 *
 * COMPLETE_INSPECTION:
 *   { "completedAtDevice": "2026-10-22T10:00:00" }
 */
@Getter
@Setter
public class SyncItemRequest {

    /**
     * Chave idempotente gerada pelo dispositivo (UUID).
     * Reenvios com a mesma chave são ignorados silenciosamente.
     */
    @NotBlank(message = "idempotencyKey é obrigatório")
    private String idempotencyKey;

    /** ID da inspeção à qual esta operação pertence. */
    @NotNull(message = "inspectionId é obrigatório")
    private Long inspectionId;

    /** Tipo da operação — define como o payload será interpretado. */
    @NotNull(message = "operationType é obrigatório")
    private SyncOperationType operationType;

    /** Payload JSON cru da operação. Formato varia conforme operationType. */
    @NotBlank(message = "payload é obrigatório")
    private String payload;
}

package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.SyncOperationType;
import com.experiencefieldops.api.enums.SyncStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Resultado individual de uma operação processada pelo endpoint POST /api/v1/sync.
 * Integra as listas synced[] e failed[] da SyncResponse.
 */
@Getter
@Builder
public class SyncItemResult {

    private String idempotencyKey;
    private Long inspectionId;
    private SyncOperationType operationType;
    private SyncStatus status;

    /** Id criado no servidor: resposta (SAVE_RESPONSE), NC (SAVE_NON_CONFORMITY) ou a inspeção (START/COMPLETE). Também em SKIPPED. */
    private Long entityId;

    /** Presente apenas quando status == FAILED. */
    private String errorMessage;

    // ── factories ─────────────────────────────────────────────────────────────

    public static SyncItemResult success(String key, Long inspectionId, SyncOperationType type, Long entityId) {
        return SyncItemResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.SUCCESS)
                .entityId(entityId)
                .build();
    }

    public static SyncItemResult skipped(String key, Long inspectionId, SyncOperationType type, Long entityId) {
        return SyncItemResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.SKIPPED)
                .entityId(entityId)
                .build();
    }

    public static SyncItemResult failed(String key, Long inspectionId, SyncOperationType type,
            String errorMessage) {
        return SyncItemResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
    }
}

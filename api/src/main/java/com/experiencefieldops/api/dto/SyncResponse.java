package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.experiencefieldops.api.enums.SyncLogStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Resposta do endpoint POST /api/v1/sync.
 *
 * Retorna dois arrays:
 *   synced[] — operações processadas com êxito (SUCCESS) ou puladas por idempotência (SKIPPED)
 *   failed[] — operações que falharam com a mensagem de erro individual
 *
 * O campo status reflete o resultado consolidado do lote:
 *   SUCCESS  — todas sincronizadas
 *   PARTIAL  — mix de sucesso e falha
 *   FAILED   — todas falharam
 */
@Getter
@Builder
public class SyncResponse {

    /** Status consolidado do lote. */
    private SyncLogStatus status;

    /** Total de operações recebidas. */
    private int total;

    /** Operações processadas com êxito ou ignoradas por idempotência. */
    private List<SyncItemResult> synced;

    /** Operações que falharam. */
    private List<SyncItemResult> failed;

    /** Timestamp do processamento no servidor. */
    private LocalDateTime processedAt;
}

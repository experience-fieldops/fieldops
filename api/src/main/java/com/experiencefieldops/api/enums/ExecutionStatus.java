package com.experiencefieldops.api.enums;

/**
 * Status da execução de uma inspeção em campo.
 */
public enum ExecutionStatus {
    IN_PROGRESS,    // Técnico iniciou a execução
    PAUSED,         // Execução pausada pelo técnico
    COMPLETED,      // Execução finalizada com sucesso
    CANCELLED       // Execução cancelada
}

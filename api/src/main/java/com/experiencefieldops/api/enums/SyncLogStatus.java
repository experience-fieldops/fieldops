package com.experiencefieldops.api.enums;

/**
 * Status consolidado de um lote de sincronização (SyncLog).
 *
 * SUCCESS  — todas as operações do lote foram processadas com êxito.
 * PARTIAL  — ao menos uma operação falhou e ao menos uma teve êxito.
 * FAILED   — todas as operações do lote falharam.
 */
public enum SyncLogStatus {
    SUCCESS,
    PARTIAL,
    FAILED
}

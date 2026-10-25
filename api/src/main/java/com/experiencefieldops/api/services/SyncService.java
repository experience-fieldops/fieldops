package com.experiencefieldops.api.services;

import java.util.List;

import com.experiencefieldops.api.dto.PendingSyncInspectionResponse;
import com.experiencefieldops.api.dto.SyncBatchRequest;
import com.experiencefieldops.api.dto.SyncBatchResponse;
import com.experiencefieldops.api.dto.SyncRequest;
import com.experiencefieldops.api.dto.SyncResponse;
import com.experiencefieldops.api.dto.SyncStatusResponse;

public interface SyncService {

    /** Endpoint legado — mantido para retrocompatibilidade. */
    SyncBatchResponse processBatch(SyncBatchRequest request, Long technicianId);

    SyncStatusResponse getSyncStatus(Long inspectionId);

    /**
     * POST /api/v1/sync — novo endpoint da issue #27.
     * Processa lote offline e retorna { synced: [], failed: [] }.
     */
    SyncResponse sync(SyncRequest request, Long technicianId);

    /**
     * GET /api/v1/my-orders/pending-sync — issue #27.
     * Retorna inspeções SCHEDULED e IN_PROGRESS do técnico
     * com template completo para download offline.
     */
    List<PendingSyncInspectionResponse> getPendingSync(Long technicianId);
}

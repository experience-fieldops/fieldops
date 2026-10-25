package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.PendingSyncInspectionResponse;
import com.experiencefieldops.api.dto.SyncBatchRequest;
import com.experiencefieldops.api.dto.SyncBatchResponse;
import com.experiencefieldops.api.dto.SyncRequest;
import com.experiencefieldops.api.dto.SyncResponse;
import com.experiencefieldops.api.dto.SyncStatusResponse;
import com.experiencefieldops.api.services.SyncService;
import com.experiencefieldops.api.util.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Sincronização", description = "Endpoints de sincronização offline para o app mobile")
public class SyncController {

    private final SyncService syncService;

    // ── Endpoint novo — Issue #27 ─────────────────────────────────────────────

    /**
     * POST /api/v1/sync
     *
     * Recebe em lote dados coletados offline (execuções, respostas, NCs, evidências).
     * Processa cada operação com idempotência (reenvios ignorados).
     * Retorna { synced: [...], failed: [...] } com o resultado individual de cada item.
     *
     * Sempre retorna HTTP 200 — falhas parciais ficam em failed[].
     * TODO: technicianId virá do JWT.
     */
    @Operation(summary = "Sincronizar dados offline",
            description = "Recebe lote offline e retorna { synced: [], failed: [] } com SyncLog registrado")
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<SyncResponse>> sync(
            @Valid @RequestBody SyncRequest request,
            @RequestParam Long technicianId) {

        SyncResponse response = syncService.sync(request, technicianId);

        String msg = "Lote processado: " + response.getSynced().size()
                + " sincronizados, " + response.getFailed().size() + " falhas";

        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }

    /**
     * GET /api/v1/my-orders/pending-sync
     *
     * Retorna inspeções SCHEDULED e IN_PROGRESS atribuídas ao técnico,
     * com template completo (seções + itens) para download offline.
     * TODO: technicianId virá do JWT.
     */
    @Operation(summary = "Ordens disponíveis para download offline",
            description = "Lista inspeções SCHEDULED e IN_PROGRESS do técnico com template completo")
    @GetMapping("/my-orders/pending-sync")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PendingSyncInspectionResponse>>> getPendingSync(
            @RequestParam Long technicianId) {

        return ResponseEntity.ok(ApiResponse.success(syncService.getPendingSync(technicianId)));
    }

    // ── Endpoints legados — mantidos para retrocompatibilidade ────────────────

    /**
     * POST /api/v1/sync/batch
     * Endpoint original — mantido. Prefira usar POST /api/v1/sync para novos clientes.
     */
    @Operation(summary = "Sincronizar lote (legado)", description = "Use POST /api/v1/sync para novos clientes")
    @PostMapping("/sync/batch")
    public ResponseEntity<ApiResponse<SyncBatchResponse>> processBatch(
            @Valid @RequestBody SyncBatchRequest request,
            @RequestParam Long technicianId) {

        SyncBatchResponse response = syncService.processBatch(request, technicianId);

        return ResponseEntity.ok(ApiResponse.success(response,
                "Lote processado: " + response.getSucceeded() + " ok, "
                        + response.getFailed() + " falhas, "
                        + response.getSkipped() + " ignoradas"));
    }

    /**
     * GET /api/v1/inspections/{id}/sync-status
     */
    @Operation(summary = "Status de sincronização de uma inspeção")
    @GetMapping("/inspections/{id}/sync-status")
    public ResponseEntity<ApiResponse<SyncStatusResponse>> getSyncStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(syncService.getSyncStatus(id)));
    }
}

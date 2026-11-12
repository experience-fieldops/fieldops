package com.experiencefieldops.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.TechnicianMetricsResponse;
import com.experiencefieldops.api.services.InspectionService;
import com.experiencefieldops.api.util.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
@Tag(name = "Técnicos")
public class TechnicianController {

    private final InspectionService inspectionService;

    /**
     * GET /api/v1/technicians/{id}/metrics
     * shortcut: qualquer usuário autenticado consulta qualquer técnico, como em /inspections/assigned;
     * restringir ao próprio técnico quando o usuário vier do JWT (B-08).
     */
    @GetMapping("/{id}/metrics")
    @Operation(summary = "Métricas do técnico (concluídas, % aprovação, devolvidas)")
    public ResponseEntity<ApiResponse<TechnicianMetricsResponse>> getMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.getTechnicianMetrics(id)));
    }
}

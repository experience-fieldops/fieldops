package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.ChecklistResponseDTO;
import com.experiencefieldops.api.dto.ChecklistResponseRequest;
import com.experiencefieldops.api.dto.CompleteExecutionRequest;
import com.experiencefieldops.api.dto.ExecutionResponse;
import com.experiencefieldops.api.dto.StartExecutionRequest;
import com.experiencefieldops.api.services.InspectionExecutionService;
import com.experiencefieldops.api.util.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Execução de Inspeção", description = "Registro da execução real da inspeção em campo (TECNICO)")
public class InspectionExecutionController {

    private final InspectionExecutionService executionService;

    // ═════════════════════════════════════════════════════════════════════════
    // EXECUTION ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Iniciar execução", description = "Técnico inicia a execução da inspeção em campo com GPS")
    @PostMapping("/inspections/{inspectionId}/executions")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<ExecutionResponse>> start(
            @PathVariable Long inspectionId,
            @Valid @RequestBody StartExecutionRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        executionService.start(inspectionId, request, requestingUserId),
                        "Execução iniciada com sucesso"));
    }

    @Operation(summary = "Finalizar execução", description = "Técnico finaliza a execução com GPS de fim e observações")
    @PatchMapping("/executions/{executionId}/complete")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<ExecutionResponse>> complete(
            @PathVariable Long executionId,
            @Valid @RequestBody CompleteExecutionRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.ok(ApiResponse.success(
                executionService.complete(executionId, request, requestingUserId),
                "Execução finalizada com sucesso"));
    }

    @Operation(summary = "Pausar execução", description = "Técnico pausa a execução temporariamente")
    @PatchMapping("/executions/{executionId}/pause")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<ExecutionResponse>> pause(
            @PathVariable Long executionId,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.ok(ApiResponse.success(
                executionService.pause(executionId, requestingUserId),
                "Execução pausada"));
    }

    @Operation(summary = "Buscar execução por ID")
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<ApiResponse<ExecutionResponse>> findById(@PathVariable Long executionId) {
        return ResponseEntity.ok(ApiResponse.success(executionService.findById(executionId)));
    }

    @Operation(summary = "Listar execuções de uma inspeção")
    @GetMapping("/inspections/{inspectionId}/executions")
    public ResponseEntity<ApiResponse<List<ExecutionResponse>>> findByInspection(@PathVariable Long inspectionId) {
        return ResponseEntity.ok(ApiResponse.success(executionService.findByInspectionId(inspectionId)));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CHECKLIST RESPONSE ENDPOINTS
    // ═════════════════════════════════════════════════════════════════════════

    @Operation(summary = "Registrar resposta do checklist", description = "Técnico responde um item do checklist com GPS")
    @PostMapping("/executions/{executionId}/responses")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChecklistResponseDTO>> saveResponse(
            @PathVariable Long executionId,
            @Valid @RequestBody ChecklistResponseRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        executionService.saveResponse(executionId, request, requestingUserId),
                        "Resposta registrada com sucesso"));
    }

    @Operation(summary = "Listar respostas do checklist")
    @GetMapping("/executions/{executionId}/responses")
    public ResponseEntity<ApiResponse<List<ChecklistResponseDTO>>> findResponses(
            @PathVariable Long executionId) {

        return ResponseEntity.ok(ApiResponse.success(executionService.findResponses(executionId)));
    }
}

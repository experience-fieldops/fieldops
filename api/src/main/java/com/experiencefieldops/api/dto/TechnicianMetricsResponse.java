package com.experiencefieldops.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Métricas do técnico sobre todo o histórico de inspeções atribuídas (B-07).")
public class TechnicianMetricsResponse {

    private Long technicianId;

    @Schema(description = "Concluídas = COMPLETED + UNDER_REVIEW + APPROVED.")
    private long completedCount;

    @Schema(description = "Inspeções APPROVED.")
    private long approvedCount;

    @Schema(description = "Devolvidas = REJECTED.")
    private long rejectedCount;

    @Schema(description = "approvedCount / completedCount * 100, escala 0–100 com uma casa decimal. 0 se completedCount = 0.",
            example = "75.0")
    private double approvalRate;
}

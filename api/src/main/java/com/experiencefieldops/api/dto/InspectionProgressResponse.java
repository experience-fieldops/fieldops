package com.experiencefieldops.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Progresso de preenchimento do checklist de uma inspeção (B-03).")
public class InspectionProgressResponse {

    private Long inspectionId;

    @Schema(description = "Total de itens do checklist (snapshot da inspeção).")
    private int totalItems;

    @Schema(description = "Itens respondidos com valor (não inclui os marcados como não aplicáveis).")
    private int answeredItems;

    @Schema(description = "Itens marcados como não aplicáveis (notApplicable = true).")
    private int notApplicableItems;

    @Schema(description = "totalItems - notApplicableItems.")
    private int applicableItems;

    @Schema(description = "applicableItems - answeredItems (mínimo 0).")
    private int pendingItems;

    @Schema(description = "Escala 0–100, uma casa decimal: answeredItems / applicableItems * 100. "
            + "Itens não aplicáveis saem do denominador (não contam como respondidos nem como pendentes). "
            + "Se applicableItems = 0, retorna 100.",
            example = "66.7")
    private double progressPercentage;
}

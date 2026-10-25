package com.experiencefieldops.api.dto;

import java.time.LocalDate;
import java.util.List;

import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.enums.InspectionStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Payload retornado por GET /api/v1/my-orders/pending-sync.
 *
 * Contém os dados necessários para o app mobile fazer o download da inspeção
 * e trabalhar offline: metadados da inspeção + template completo (seções e itens).
 */
@Getter
@Builder
public class PendingSyncInspectionResponse {

    private Long id;
    private String clientName;
    private String siteName;
    private String siteAddress;
    private String equipmentName;
    private String equipmentQrCode;
    private LocalDate scheduledDate;
    private InspectionStatus status;

    /** Versão do template com seções e itens — necessário para montar o checklist offline. */
    private Long templateVersionId;
    private String templateTitle;
    private List<TemplateSectionResponse> sections;

    public static PendingSyncInspectionResponse from(Inspection inspection,
            List<TemplateSectionResponse> sections) {
        return PendingSyncInspectionResponse.builder()
                .id(inspection.getId())
                .clientName(inspection.getClient().getName())
                .siteName(inspection.getSite().getName())
                .siteAddress(inspection.getSite().getAddress())
                .equipmentName(inspection.getEquipment() != null
                        ? inspection.getEquipment().getName() : null)
                .equipmentQrCode(inspection.getEquipment() != null
                        ? inspection.getEquipment().getQrCode() : null)
                .scheduledDate(inspection.getScheduledDate())
                .status(inspection.getStatus())
                .templateVersionId(inspection.getTemplateVersion().getId())
                .templateTitle(inspection.getTemplateVersion().getTitleSnapshot())
                .sections(sections)
                .build();
    }
}

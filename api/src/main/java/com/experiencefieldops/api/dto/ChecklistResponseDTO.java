package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.ChecklistResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChecklistResponseDTO {

    private Long id;
    private Long executionId;
    private Long snapshotItemId;
    private String snapshotItemTitle;
    private String snapshotItemCode;
    private String value;
    private String observation;
    private Boolean notApplicable;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private LocalDateTime respondedAt;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChecklistResponseDTO from(ChecklistResponse resp) {
        return ChecklistResponseDTO.builder()
                .id(resp.getId())
                .executionId(resp.getExecution().getId())
                .snapshotItemId(resp.getSnapshotItem().getId())
                .snapshotItemTitle(resp.getSnapshotItem().getTitle())
                .snapshotItemCode(resp.getSnapshotItem().getCode())
                .value(resp.getValue())
                .observation(resp.getObservation())
                .notApplicable(resp.getNotApplicable())
                .gpsLatitude(resp.getGpsLatitude())
                .gpsLongitude(resp.getGpsLongitude())
                .respondedAt(resp.getRespondedAt())
                .version(resp.getVersion())
                .createdAt(resp.getCreatedAt())
                .updatedAt(resp.getUpdatedAt())
                .build();
    }
}

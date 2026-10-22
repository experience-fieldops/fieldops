package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.InspectionExecution;
import com.experiencefieldops.api.enums.ExecutionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExecutionResponse {

    private Long id;
    private Long inspectionId;
    private Long technicianId;
    private String technicianName;
    private ExecutionStatus status;
    private Double startGpsLatitude;
    private Double startGpsLongitude;
    private Double endGpsLatitude;
    private Double endGpsLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String notes;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExecutionResponse from(InspectionExecution exec) {
        return ExecutionResponse.builder()
                .id(exec.getId())
                .inspectionId(exec.getInspection().getId())
                .technicianId(exec.getTechnician().getId())
                .technicianName(exec.getTechnician().getName())
                .status(exec.getStatus())
                .startGpsLatitude(exec.getStartGpsLatitude())
                .startGpsLongitude(exec.getStartGpsLongitude())
                .endGpsLatitude(exec.getEndGpsLatitude())
                .endGpsLongitude(exec.getEndGpsLongitude())
                .startedAt(exec.getStartedAt())
                .completedAt(exec.getCompletedAt())
                .notes(exec.getNotes())
                .version(exec.getVersion())
                .createdAt(exec.getCreatedAt())
                .updatedAt(exec.getUpdatedAt())
                .build();
    }
}

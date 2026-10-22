package com.experiencefieldops.api.services;

import java.util.List;

import com.experiencefieldops.api.dto.ChecklistResponseDTO;
import com.experiencefieldops.api.dto.ChecklistResponseRequest;
import com.experiencefieldops.api.dto.CompleteExecutionRequest;
import com.experiencefieldops.api.dto.ExecutionResponse;
import com.experiencefieldops.api.dto.StartExecutionRequest;

public interface InspectionExecutionService {

    ExecutionResponse start(Long inspectionId, StartExecutionRequest request, Long technicianId);

    ExecutionResponse complete(Long executionId, CompleteExecutionRequest request, Long technicianId);

    ExecutionResponse pause(Long executionId, Long technicianId);

    ExecutionResponse findById(Long executionId);

    List<ExecutionResponse> findByInspectionId(Long inspectionId);

    ChecklistResponseDTO saveResponse(Long executionId, ChecklistResponseRequest request, Long technicianId);

    List<ChecklistResponseDTO> findResponses(Long executionId);
}

package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.ChecklistResponseDTO;
import com.experiencefieldops.api.dto.ChecklistResponseRequest;
import com.experiencefieldops.api.dto.CompleteExecutionRequest;
import com.experiencefieldops.api.dto.ExecutionResponse;
import com.experiencefieldops.api.dto.StartExecutionRequest;
import com.experiencefieldops.api.entities.ChecklistResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionExecution;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.ExecutionStatus;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ChecklistResponseRepository;
import com.experiencefieldops.api.repositories.InspectionExecutionRepository;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.InspectionExecutionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionExecutionServiceImpl implements InspectionExecutionService {

    private final InspectionExecutionRepository executionRepository;
    private final ChecklistResponseRepository checklistResponseRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;

    // ═════════════════════════════════════════════════════════════════════════
    // EXECUTION
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ExecutionResponse start(Long inspectionId, StartExecutionRequest request, Long technicianId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspeção não encontrada: " + inspectionId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado: " + technicianId));

        // Apenas TECNICO pode executar
        if (technician.getRole() != UserRole.TECNICO) {
            throw new ForbiddenException("Apenas técnicos podem iniciar execução de inspeção");
        }

        // Deve ser o técnico atribuído à inspeção
        if (!inspection.getAssignedTo().getId().equals(technicianId)) {
            throw new ForbiddenException("Apenas o técnico atribuído pode executar esta inspeção");
        }

        // Inspeção deve estar SCHEDULED ou IN_PROGRESS
        if (inspection.getStatus() != InspectionStatus.SCHEDULED
                && inspection.getStatus() != InspectionStatus.IN_PROGRESS) {
            throw new ConflictException(
                    "Inspeção deve estar agendada ou em andamento para iniciar execução. Status atual: "
                            + inspection.getStatus());
        }

        // Não pode haver execução em andamento
        if (executionRepository.existsByInspectionIdAndStatus(inspectionId, ExecutionStatus.IN_PROGRESS)) {
            throw new ConflictException("Já existe uma execução em andamento para esta inspeção");
        }

        // Atualiza status da inspeção para IN_PROGRESS se ainda SCHEDULED
        if (inspection.getStatus() == InspectionStatus.SCHEDULED) {
            inspection.setStatus(InspectionStatus.IN_PROGRESS);
            inspection.setStartedAtServer(LocalDateTime.now());
            inspectionRepository.save(inspection);
        }

        InspectionExecution execution = InspectionExecution.builder()
                .inspection(inspection)
                .technician(technician)
                .status(ExecutionStatus.IN_PROGRESS)
                .startGpsLatitude(request.getGpsLatitude())
                .startGpsLongitude(request.getGpsLongitude())
                .startedAt(parseDateTime(request.getStartedAt(), LocalDateTime.now()))
                .build();

        return ExecutionResponse.from(executionRepository.save(execution));
    }

    @Override
    @Transactional
    public ExecutionResponse complete(Long executionId, CompleteExecutionRequest request, Long technicianId) {
        InspectionExecution execution = getExecutionAndValidateTechnician(executionId, technicianId);

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS
                && execution.getStatus() != ExecutionStatus.PAUSED) {
            throw new ConflictException(
                    "Execução deve estar em andamento ou pausada para finalizar. Status atual: "
                            + execution.getStatus());
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setEndGpsLatitude(request.getEndGpsLatitude());
        execution.setEndGpsLongitude(request.getEndGpsLongitude());
        execution.setCompletedAt(parseDateTime(request.getCompletedAt(), LocalDateTime.now()));
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            execution.setNotes(request.getNotes());
        }

        InspectionExecution saved = executionRepository.save(execution);

        // Atualiza a inspeção para COMPLETED
        Inspection inspection = saved.getInspection();
        if (inspection.getStatus() == InspectionStatus.IN_PROGRESS) {
            inspection.setStatus(InspectionStatus.COMPLETED);
            inspection.setCompletedAtServer(saved.getCompletedAt());
            inspectionRepository.save(inspection);
        }

        return ExecutionResponse.from(saved);
    }

    @Override
    @Transactional
    public ExecutionResponse pause(Long executionId, Long technicianId) {
        InspectionExecution execution = getExecutionAndValidateTechnician(executionId, technicianId);

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new ConflictException(
                    "Execução deve estar em andamento para pausar. Status atual: " + execution.getStatus());
        }

        execution.setStatus(ExecutionStatus.PAUSED);
        return ExecutionResponse.from(executionRepository.save(execution));
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionResponse findById(Long executionId) {
        return ExecutionResponse.from(executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execução não encontrada: " + executionId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> findByInspectionId(Long inspectionId) {
        return executionRepository.findByInspectionIdOrderByStartedAtDesc(inspectionId).stream()
                .map(ExecutionResponse::from)
                .toList();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CHECKLIST RESPONSES
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ChecklistResponseDTO saveResponse(Long executionId, ChecklistResponseRequest request, Long technicianId) {
        InspectionExecution execution = getExecutionAndValidateTechnician(executionId, technicianId);

        if (execution.getStatus() != ExecutionStatus.IN_PROGRESS) {
            throw new ConflictException(
                    "Execução deve estar em andamento para registrar respostas. Status atual: "
                            + execution.getStatus());
        }

        // Valida que o snapshot item pertence à inspeção
        InspectionItemSnapshot snapshot = snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(
                        execution.getInspection().getId())
                .stream()
                .filter(s -> s.getId().equals(request.getSnapshotItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item do snapshot não encontrado: " + request.getSnapshotItemId()));

        // Upsert: se já existe resposta para este item, atualiza
        ChecklistResponse existing = checklistResponseRepository
                .findByExecutionIdAndSnapshotItemId(executionId, request.getSnapshotItemId())
                .orElse(null);

        boolean notApplicable = request.getNotApplicable() != null && request.getNotApplicable();

        if (existing != null) {
            existing.setValue(notApplicable ? null : request.getValue());
            existing.setObservation(request.getObservation());
            existing.setNotApplicable(notApplicable);
            existing.setGpsLatitude(request.getGpsLatitude());
            existing.setGpsLongitude(request.getGpsLongitude());
            existing.setRespondedAt(parseDateTime(request.getRespondedAt(), LocalDateTime.now()));
            return ChecklistResponseDTO.from(checklistResponseRepository.save(existing));
        }

        ChecklistResponse response = ChecklistResponse.builder()
                .execution(execution)
                .snapshotItem(snapshot)
                .value(notApplicable ? null : request.getValue())
                .observation(request.getObservation())
                .notApplicable(notApplicable)
                .gpsLatitude(request.getGpsLatitude())
                .gpsLongitude(request.getGpsLongitude())
                .respondedAt(parseDateTime(request.getRespondedAt(), LocalDateTime.now()))
                .build();

        return ChecklistResponseDTO.from(checklistResponseRepository.save(response));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistResponseDTO> findResponses(Long executionId) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execução não encontrada: " + executionId);
        }
        return checklistResponseRepository.findByExecutionIdOrderByRespondedAtAsc(executionId).stream()
                .map(ChecklistResponseDTO::from)
                .toList();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private InspectionExecution getExecutionAndValidateTechnician(Long executionId, Long technicianId) {
        InspectionExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execução não encontrada: " + executionId));

        if (!execution.getTechnician().getId().equals(technicianId)) {
            throw new ForbiddenException("Apenas o técnico responsável pela execução pode modificá-la");
        }

        return execution;
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
}

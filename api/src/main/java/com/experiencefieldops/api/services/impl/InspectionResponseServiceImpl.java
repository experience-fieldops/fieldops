package com.experiencefieldops.api.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionProgressResponse;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.ResponseResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.InspectionResponse;
import com.experiencefieldops.api.enums.ConformityValue;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.ResponseType;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ChecklistResponseRepository;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionResponseRepository;
import com.experiencefieldops.api.services.InspectionResponseService;
import com.experiencefieldops.api.util.ResponseValueValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionResponseServiceImpl implements InspectionResponseService {

    private final InspectionResponseRepository responseRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;
    private final ChecklistResponseRepository checklistResponseRepository;

    @Override
    @Transactional
    public ResponseResponse save(Long inspectionId, ResponseRequest request) {
        Inspection inspection = findEditableInspection(inspectionId);
        InspectionItemSnapshot snapshotItem = findSnapshotItem(inspectionId, request.getSnapshotItemId());

        boolean notApplicable = Boolean.TRUE.equals(request.getNotApplicable());

        if (!notApplicable) {
            validateValue(snapshotItem, request.getValue());
            validateObservation(snapshotItem, request.getValue(), request.getObservation());
        }

        // Upsert: se já existe resposta para este item, atualiza
        InspectionResponse response = responseRepository
                .findByInspectionIdAndSnapshotItemId(inspectionId, snapshotItem.getId())
                .orElse(InspectionResponse.builder()
                        .inspection(inspection)
                        .snapshotItem(snapshotItem)
                        .build());

        response.setValue(notApplicable ? null : request.getValue());
        response.setObservation(notApplicable ? null : request.getObservation());
        response.setNotApplicable(notApplicable);

        return ResponseResponse.from(responseRepository.save(response));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseResponse> findAll(Long inspectionId) {
        findInspection(inspectionId);
        return responseRepository.findByInspectionId(inspectionId)
                .stream()
                .map(ResponseResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseResponse findById(Long inspectionId, Long responseId) {
        return ResponseResponse.from(findResponse(inspectionId, responseId));
    }

    @Override
    @Transactional
    public ResponseResponse update(Long inspectionId, Long responseId, ResponseRequest request) {
        findEditableInspection(inspectionId);
        InspectionResponse response = findResponse(inspectionId, responseId);

        InspectionItemSnapshot snapshotItem = response.getSnapshotItem();
        boolean notApplicable = Boolean.TRUE.equals(request.getNotApplicable());

        if (!notApplicable) {
            validateValue(snapshotItem, request.getValue());
            validateObservation(snapshotItem, request.getValue(), request.getObservation());
        }

        response.setValue(notApplicable ? null : request.getValue());
        response.setObservation(notApplicable ? null : request.getObservation());
        response.setNotApplicable(notApplicable);

        return ResponseResponse.from(responseRepository.save(response));
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionProgressResponse getProgress(Long inspectionId) {
        findInspection(inspectionId);

        List<InspectionItemSnapshot> allItems = snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(inspectionId);

        int totalItems = allItems.size();

        // Conta por item, somando os dois fluxos de resposta (B-03): /inspections/{id}/responses + sync
        // (inspection_responses) e /executions/{id}/responses, usado pelo app (checklist_responses).
        Set<Long> answered = new HashSet<>();
        Set<Long> notApplicable = new HashSet<>();
        responseRepository.findByInspectionId(inspectionId).forEach(r ->
                (Boolean.TRUE.equals(r.getNotApplicable()) ? notApplicable : answered).add(r.getSnapshotItem().getId()));
        checklistResponseRepository.findByExecutionInspectionId(inspectionId).forEach(r ->
                (Boolean.TRUE.equals(r.getNotApplicable()) ? notApplicable : answered).add(r.getSnapshotItem().getId()));
        notApplicable.removeAll(answered); // item com valor em algum fluxo conta como respondido

        long notApplicableItems = notApplicable.size();
        long answeredItems = answered.size();

        int applicableItems = totalItems - (int) notApplicableItems;
        int pendingItems = Math.max(0, applicableItems - (int) answeredItems);
        double progressPercentage = applicableItems == 0 ? 100.0
                : Math.round((answeredItems * 100.0 / applicableItems) * 10.0) / 10.0;

        return InspectionProgressResponse.builder()
                .inspectionId(inspectionId)
                .totalItems(totalItems)
                .answeredItems((int) answeredItems)
                .notApplicableItems((int) notApplicableItems)
                .applicableItems(applicableItems)
                .pendingItems(pendingItems)
                .progressPercentage(progressPercentage)
                .build();
    }

    // ── validações ────────────────────────────────────────────────────────────

    /**
     * Valida o valor conforme o response_type do item (RN-036).
     */
    private void validateValue(InspectionItemSnapshot item, String value) {
        if (value == null || value.isBlank()) {
            if (Boolean.TRUE.equals(item.getRequired())) {
                throw new IllegalArgumentException(
                        "Item obrigatório não pode ter resposta vazia: " + item.getTitle());
            }
            return; // opcional sem valor — ok
        }

        ResponseValueValidator.validate(item, value);
    }

    /**
     * Exige observação quando o item for CONFORMITY = NON_CONFORMING
     * e observationRequiredOnFailure = true (RN-038).
     */
    private void validateObservation(InspectionItemSnapshot item, String value, String observation) {
        if (item.getResponseType() == ResponseType.CONFORMITY
                && Boolean.TRUE.equals(item.getObservationRequiredOnFailure())
                && value != null
                && value.equalsIgnoreCase(ConformityValue.NON_CONFORMING.name())) {

            if (observation == null || observation.isBlank()) {
                throw new IllegalArgumentException(
                        "Observação obrigatória para item não conforme: " + item.getTitle() + " (RN-038)");
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Retorna a inspeção somente se ainda for editável.
     * - COMPLETED, UNDER_REVIEW → bloqueado (RN-043)
     * - APPROVED → somente leitura, retorna 403 (RN-082)
     * - CANCELLED → bloqueado
     */
    private Inspection findEditableInspection(Long inspectionId) {
        Inspection inspection = findInspection(inspectionId);

        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new com.experiencefieldops.api.exception.ForbiddenException(
                    "Respostas de inspeção aprovada são somente leitura (RN-082)");
        }

        if (inspection.getStatus() == InspectionStatus.COMPLETED
                || inspection.getStatus() == InspectionStatus.UNDER_REVIEW
                || inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new ConflictException(
                    "Não é possível alterar respostas de inspeção com status: " + inspection.getStatus() + " (RN-043)");
        }

        return inspection;
    }

    private Inspection findInspection(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", inspectionId));
    }

    private InspectionItemSnapshot findSnapshotItem(Long inspectionId, Long snapshotItemId) {
        return snapshotRepository.findById(snapshotItemId)
                .filter(s -> s.getInspection().getId().equals(inspectionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InspectionItemSnapshot " + snapshotItemId + " não encontrado na inspeção " + inspectionId));
    }

    private InspectionResponse findResponse(Long inspectionId, Long responseId) {
        return responseRepository.findByInspectionIdAndId(inspectionId, responseId)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionResponse", responseId));
    }
}

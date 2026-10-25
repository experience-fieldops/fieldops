package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.dto.NonConformityRequest;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.SyncItemRequest;
import com.experiencefieldops.api.dto.SyncItemResult;
import com.experiencefieldops.api.dto.SyncOperationRequest;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.SyncOutbox;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SyncOperationType;
import com.experiencefieldops.api.enums.SyncStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.SyncOutboxRepository;
import com.experiencefieldops.api.services.AuditLogService;
import com.experiencefieldops.api.services.InspectionResponseService;
import com.experiencefieldops.api.services.InspectionService;
import com.experiencefieldops.api.services.NonConformityService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

/**
 * Processa operações individuais de sync, cada uma em sua própria transação
 * (Propagation.REQUIRES_NEW).
 *
 * Extraído do SyncServiceImpl para que o Spring AOP intercepte corretamente
 * a anotação @Transactional — chamadas internas (self-invocation) não passam
 * pelo proxy e REQUIRES_NEW seria ignorado se ficasse no mesmo bean.
 *
 * Com REQUIRES_NEW, uma falha em uma operação marca rollback apenas da sua
 * sub-transação. A transação pai do SyncServiceImpl.sync() continua válida
 * e consegue persistir o SyncLog e retornar { synced[], failed[] } normalmente.
 */
@Component
@RequiredArgsConstructor
public class SyncItemProcessor {

    private static final Logger log = LoggerFactory.getLogger(SyncItemProcessor.class);

    private final SyncOutboxRepository outboxRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionService inspectionService;
    private final InspectionResponseService responseService;
    private final NonConformityService nonConformityService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * Ponto de entrada chamado pelo SyncServiceImpl.
     * Verifica idempotência e delega para execute().
     *
     * Roda em REQUIRES_NEW: qualquer exceção faz rollback apenas desta
     * sub-transação, sem contaminar a transação pai.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncItemResult process(SyncItemRequest item, Long technicianId) {
        return outboxRepository.findByIdempotencyKey(item.getIdempotencyKey())
                .map(existing -> {
                    if (existing.getStatus() == SyncStatus.SUCCESS) {
                        return SyncItemResult.skipped(
                                item.getIdempotencyKey(),
                                item.getInspectionId(),
                                item.getOperationType());
                    }
                    // Se estava FAILED, tenta novamente
                    return execute(item, existing, technicianId);
                })
                .orElseGet(() -> {
                    Inspection inspection = inspectionRepository.findById(item.getInspectionId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Inspection", item.getInspectionId()));

                    SyncOutbox entry = SyncOutbox.builder()
                            .idempotencyKey(item.getIdempotencyKey())
                            .inspection(inspection)
                            .operationType(item.getOperationType())
                            .status(SyncStatus.PENDING)
                            .payload(item.getPayload())
                            .attemptCount(0)
                            .build();

                    return execute(item, outboxRepository.save(entry), technicianId);
                });
    }

    private SyncItemResult execute(SyncItemRequest item, SyncOutbox entry, Long technicianId) {
        entry.setAttemptCount(entry.getAttemptCount() + 1);
        entry.setProcessedAt(LocalDateTime.now());

        try {
            Inspection inspection = entry.getInspection();

            if (inspection.getStatus() == InspectionStatus.APPROVED) {
                throw new IllegalStateException(
                        "Dados aprovados no servidor não aceitam atualização tardia (RN-076)");
            }
            if (inspection.getStatus() == InspectionStatus.CANCELLED) {
                throw new IllegalStateException(
                        "Inspeção cancelada no servidor — dados locais não foram descartados (RN-077)");
            }

            SyncOperationRequest legacyOp = new SyncOperationRequest();
            legacyOp.setIdempotencyKey(item.getIdempotencyKey());
            legacyOp.setInspectionId(item.getInspectionId());
            legacyOp.setOperationType(item.getOperationType());
            legacyOp.setPayload(item.getPayload());
            dispatch(legacyOp, technicianId);

            entry.setStatus(SyncStatus.SUCCESS);
            entry.setErrorMessage(null);
            outboxRepository.save(entry);

            return SyncItemResult.success(
                    item.getIdempotencyKey(),
                    item.getInspectionId(),
                    item.getOperationType());

        } catch (Exception e) {
            log.warn("Sync falhou [key={}] [type={}]: {}",
                    item.getIdempotencyKey(), item.getOperationType(), e.getMessage());

            entry.setStatus(SyncStatus.FAILED);
            entry.setErrorMessage(e.getMessage());
            outboxRepository.save(entry);

            auditLogService.log(technicianId, null, AuditAction.STATUS_CHANGE,
                    "SyncOutbox", entry.getId(), "Falha: " + e.getMessage());

            return SyncItemResult.failed(
                    item.getIdempotencyKey(),
                    item.getInspectionId(),
                    item.getOperationType(),
                    e.getMessage());
        }
    }

    private void dispatch(SyncOperationRequest op, Long technicianId) throws Exception {
        switch (op.getOperationType()) {
            case START_INSPECTION -> {
                InspectionStartRequest req = objectMapper.readValue(
                        op.getPayload(), InspectionStartRequest.class);
                inspectionService.start(op.getInspectionId(), req, technicianId);
            }
            case SAVE_RESPONSE -> {
                ResponseRequest req = objectMapper.readValue(
                        op.getPayload(), ResponseRequest.class);
                responseService.save(op.getInspectionId(), req);
            }
            case SAVE_NON_CONFORMITY -> {
                NonConformityRequest req = objectMapper.readValue(
                        op.getPayload(), NonConformityRequest.class);
                nonConformityService.create(op.getInspectionId(), req);
            }
            case UPLOAD_EVIDENCE -> {
                log.info("UPLOAD_EVIDENCE rastreado na outbox — arquivo enviado via endpoint dedicado");
            }
            case COMPLETE_INSPECTION -> {
                InspectionCompleteRequest req = objectMapper.readValue(
                        op.getPayload(), InspectionCompleteRequest.class);
                inspectionService.complete(op.getInspectionId(), req, technicianId);
            }
            default -> throw new IllegalArgumentException(
                    "Tipo de operação desconhecido: " + op.getOperationType());
        }
    }
}

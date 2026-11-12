package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.experiencefieldops.api.dto.NonConformityResponse;
import com.experiencefieldops.api.dto.SyncItemRequest;
import com.experiencefieldops.api.dto.SyncItemResult;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.SyncOutbox;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SyncOperationType;
import com.experiencefieldops.api.enums.SyncStatus;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.SyncOutboxRepository;
import com.experiencefieldops.api.services.impl.SyncItemProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SyncItemProcessorTest {

    @Mock private SyncOutboxRepository outboxRepository;
    @Mock private InspectionRepository inspectionRepository;
    @Mock private InspectionService inspectionService;
    @Mock private InspectionResponseService responseService;
    @Mock private NonConformityService nonConformityService;
    @Mock private AuditLogService auditLogService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private SyncItemProcessor processor;

    private static SyncItemRequest ncItem() {
        SyncItemRequest item = new SyncItemRequest();
        item.setIdempotencyKey("k1");
        item.setInspectionId(1L);
        item.setOperationType(SyncOperationType.SAVE_NON_CONFORMITY);
        item.setPayload("{\"title\":\"Vazamento\",\"severity\":\"HIGH\"}");
        return item;
    }

    @Test
    void process_devolveIdDaNcCriadaEGuardaNaOutbox() {
        when(outboxRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(
                Inspection.builder().id(1L).status(InspectionStatus.IN_PROGRESS).build()));
        when(outboxRepository.save(any(SyncOutbox.class))).thenAnswer(inv -> inv.getArgument(0));
        when(nonConformityService.create(eq(1L), any())).thenReturn(NonConformityResponse.builder().id(77L).build());

        SyncItemResult result = processor.process(ncItem(), 10L);

        assertThat(result.getStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(result.getEntityId()).isEqualTo(77L);
    }

    @Test
    void process_replayJaSincronizadoDevolveSkippedComOMesmoId() {
        when(outboxRepository.findByIdempotencyKey("k1")).thenReturn(Optional.of(
                SyncOutbox.builder().idempotencyKey("k1").status(SyncStatus.SUCCESS).entityId(77L).build()));

        SyncItemResult result = processor.process(ncItem(), 10L);

        assertThat(result.getStatus()).isEqualTo(SyncStatus.SKIPPED);
        assertThat(result.getEntityId()).isEqualTo(77L);
        verify(nonConformityService, never()).create(any(), any());
    }
}

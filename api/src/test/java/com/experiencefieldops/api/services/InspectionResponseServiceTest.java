package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.experiencefieldops.api.dto.InspectionProgressResponse;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.ResponseResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.InspectionResponse;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.ResponseType;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionResponseRepository;
import com.experiencefieldops.api.services.impl.InspectionResponseServiceImpl;

@ExtendWith(MockitoExtension.class)
class InspectionResponseServiceTest {

    @Mock private InspectionResponseRepository responseRepository;
    @Mock private InspectionRepository inspectionRepository;
    @Mock private InspectionItemSnapshotRepository snapshotRepository;

    @InjectMocks
    private InspectionResponseServiceImpl service;

    private Inspection inspection(Long id, InspectionStatus status) {
        Inspection i = new Inspection();
        i.setId(id);
        i.setStatus(status);
        return i;
    }

    private InspectionItemSnapshot snapshot(Long id, Inspection insp, ResponseType type,
            boolean required, boolean obsRequired) {
        return InspectionItemSnapshot.builder()
                .id(id).inspection(insp).title("Pressão de operação")
                .responseType(type).required(required)
                .observationRequiredOnFailure(obsRequired)
                .evidenceRequiredOnFailure(false)
                .build();
    }

    private ResponseRequest request(Long snapshotItemId, String value, String obs, Boolean na) {
        ResponseRequest r = new ResponseRequest();
        r.setSnapshotItemId(snapshotItemId);
        r.setValue(value);
        r.setObservation(obs);
        r.setNotApplicable(na);
        return r;
    }

    // ── save (upsert) ─────────────────────────────────────────────────────────

    @Test
    void save_deveCriarRespostaValidaDoTipoNumber() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(snapshotRepository.findById(5L)).thenReturn(Optional.of(snap));
        when(responseRepository.findByInspectionIdAndSnapshotItemId(1L, 5L))
                .thenReturn(Optional.empty());
        when(responseRepository.save(any(InspectionResponse.class)))
                .thenAnswer(inv -> { InspectionResponse r = inv.getArgument(0); r.setId(50L); return r; });

        ResponseResponse response = service.save(1L, request(5L, "7.5", null, false));

        assertThat(response).isNotNull();
        verify(responseRepository).save(any(InspectionResponse.class));
    }

    @Test
    void save_deveFalharComValorInvalidoParaNumber() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(snapshotRepository.findById(5L)).thenReturn(Optional.of(snap));

        assertThatThrownBy(() -> service.save(1L, request(5L, "abc", null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NUMBER");

        verify(responseRepository, never()).save(any());
    }

    @Test
    void save_deveBloquearQuandoInspecaoAprovada() {
        Inspection insp = inspection(1L, InspectionStatus.APPROVED);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        assertThatThrownBy(() -> service.save(1L, request(5L, "7.5", null, false)))
                .isInstanceOf(ForbiddenException.class);

        verify(responseRepository, never()).save(any());
    }

    @Test
    void save_deveBloquearQuandoInspecaoCompleted() {
        Inspection insp = inspection(1L, InspectionStatus.COMPLETED);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        assertThatThrownBy(() -> service.save(1L, request(5L, "7.5", null, false)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void save_devePermitirNaoAplicavelSemValidarValor() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(snapshotRepository.findById(5L)).thenReturn(Optional.of(snap));
        when(responseRepository.findByInspectionIdAndSnapshotItemId(1L, 5L))
                .thenReturn(Optional.empty());
        when(responseRepository.save(any(InspectionResponse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // value inválido, mas notApplicable=true → não valida o valor
        ResponseResponse response = service.save(1L, request(5L, "abc", null, true));

        assertThat(response).isNotNull();
        verify(responseRepository).save(any(InspectionResponse.class));
    }

    // ── getProgress ───────────────────────────────────────────────────────────

    @Test
    void getProgress_deveCalcularPercentualCorretamente() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        // 4 itens no snapshot
        List<InspectionItemSnapshot> itens = List.of(
                snapshot(1L, insp, ResponseType.NUMBER, true, false),
                snapshot(2L, insp, ResponseType.NUMBER, true, false),
                snapshot(3L, insp, ResponseType.NUMBER, true, false),
                snapshot(4L, insp, ResponseType.NUMBER, true, false));
        when(snapshotRepository.findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(1L))
                .thenReturn(itens);

        // 2 respostas: 1 respondida + 1 não aplicável
        InspectionResponse respondida = InspectionResponse.builder()
                .id(1L).notApplicable(false).value("10").build();
        InspectionResponse na = InspectionResponse.builder()
                .id(2L).notApplicable(true).build();
        when(responseRepository.findByInspectionId(1L)).thenReturn(List.of(respondida, na));

        InspectionProgressResponse progress = service.getProgress(1L);

        // total=4, NA=1 → aplicáveis=3, respondidos=1 → 33.3%
        assertThat(progress.getTotalItems()).isEqualTo(4);
        assertThat(progress.getNotApplicableItems()).isEqualTo(1);
        assertThat(progress.getApplicableItems()).isEqualTo(3);
        assertThat(progress.getAnsweredItems()).isEqualTo(1);
        assertThat(progress.getProgressPercentage()).isEqualTo(33.3);
    }

    // ── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_deveRetornarRespostasDaInspecao() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);
        InspectionResponse r = InspectionResponse.builder()
                .id(1L).inspection(insp).snapshotItem(snap).value("10").notApplicable(false).build();
        when(responseRepository.findByInspectionId(1L)).thenReturn(List.of(r));

        List<ResponseResponse> result = service.findAll(1L);

        assertThat(result).hasSize(1);
    }

    // ── validações adicionais ──────────────────────────────────────────────

    @Test
    void save_deveExigirObservacaoEmNaoConformidade() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        // CONFORMITY com observação obrigatória em falha
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.CONFORMITY, true, true);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(snapshotRepository.findById(5L)).thenReturn(Optional.of(snap));

        // NON_CONFORMING sem observação → deve falhar (RN-038)
        assertThatThrownBy(() -> service.save(1L, request(5L, "NON_CONFORMING", null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Observação");

        verify(responseRepository, never()).save(any());
    }

    @Test
    void save_deveFalharComValorInvalidoParaBoolean() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.BOOLEAN, true, false);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(snapshotRepository.findById(5L)).thenReturn(Optional.of(snap));

        assertThatThrownBy(() -> service.save(1L, request(5L, "talvez", null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BOOLEAN");
    }

    @Test
    void findById_deveRetornarResposta() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);
        InspectionResponse r = InspectionResponse.builder()
                .id(7L).inspection(insp).snapshotItem(snap).value("10").notApplicable(false).build();
        when(responseRepository.findByInspectionIdAndId(1L, 7L)).thenReturn(Optional.of(r));

        ResponseResponse response = service.findById(1L, 7L);

        assertThat(response.getId()).isEqualTo(7L);
    }

    @Test
    void update_deveAtualizarRespostaExistente() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS);
        InspectionItemSnapshot snap = snapshot(5L, insp, ResponseType.NUMBER, true, false);
        InspectionResponse existing = InspectionResponse.builder()
                .id(7L).inspection(insp).snapshotItem(snap).value("10").notApplicable(false).build();

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(responseRepository.findByInspectionIdAndId(1L, 7L)).thenReturn(Optional.of(existing));
        when(responseRepository.save(any(InspectionResponse.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ResponseResponse response = service.update(1L, 7L, request(5L, "25.5", null, false));

        assertThat(response).isNotNull();
        assertThat(existing.getValue()).isEqualTo("25.5");
    }
}

package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.experiencefieldops.api.dto.InspectionCancelRequest;
import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionRequest;
import com.experiencefieldops.api.dto.InspectionResponse;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.enums.EquipmentStatus;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SiteStatus;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.repositories.EquipmentRepository;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionSiteRepository;
import com.experiencefieldops.api.repositories.InspectionTemplateVersionRepository;
import com.experiencefieldops.api.repositories.TemplateItemRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.impl.InspectionServiceImpl;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock private InspectionRepository inspectionRepository;
    @Mock private InspectionItemSnapshotRepository snapshotRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private InspectionSiteRepository siteRepository;
    @Mock private UserRepository userRepository;
    @Mock private InspectionTemplateVersionRepository versionRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private TemplateSectionRepository sectionRepository;
    @Mock private TemplateItemRepository itemRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private InspectionServiceImpl service;

    private User tecnico(Long id) {
        return User.builder().id(id).name("Carlos").email("tec@fieldops.com")
                .role(UserRole.TECNICO).active(true).build();
    }

    private Inspection inspection(Long id, InspectionStatus status, User assignedTo) {
        Inspection i = new Inspection();
        i.setId(id);
        i.setStatus(status);
        i.setAssignedTo(assignedTo);
        // Relações exigidas por InspectionResponse.from(...)
        i.setClient(Client.builder().id(1L).name("Empresa ABC").build());
        i.setSite(InspectionSite.builder().id(1L).name("Filial SP").build());
        i.setTemplateVersion(InspectionTemplateVersion.builder()
                .id(1L).versionNumber(1).titleSnapshot("Template").build());
        return i;
    }

    // ── start ─────────────────────────────────────────────────────────────────

    @Test
    void start_deveIniciarQuandoAgendadaETecnicoCorreto() {
        User tec = tecnico(10L);
        Inspection insp = inspection(1L, InspectionStatus.SCHEDULED, tec);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        InspectionStartRequest req = new InspectionStartRequest();
        req.setStartedAtDevice(LocalDateTime.now());

        InspectionResponse response = service.start(1L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.IN_PROGRESS);
        assertThat(insp.getStartedAtServer()).isNotNull();
        verify(auditLogService).log(eq(10L), anyString(), eq(AuditAction.STATUS_CHANGE),
                eq("Inspection"), eq(1L), anyString());
    }

    @Test
    void start_deveFalharSeNaoAgendada() {
        User tec = tecnico(10L);
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS, tec);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionStartRequest req = new InspectionStartRequest();
        req.setStartedAtDevice(LocalDateTime.now());

        assertThatThrownBy(() -> service.start(1L, req, 10L))
                .isInstanceOf(ConflictException.class);
        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void start_deveFalharSeTecnicoNaoForOResponsavel() {
        User tec = tecnico(10L);
        Inspection insp = inspection(1L, InspectionStatus.SCHEDULED, tec);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionStartRequest req = new InspectionStartRequest();
        req.setStartedAtDevice(LocalDateTime.now());

        assertThatThrownBy(() -> service.start(1L, req, 99L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void start_deveFalharSeInspecaoNaoExiste() {
        when(inspectionRepository.findById(404L)).thenReturn(Optional.empty());

        InspectionStartRequest req = new InspectionStartRequest();
        req.setStartedAtDevice(LocalDateTime.now());

        assertThatThrownBy(() -> service.start(404L, req, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── cancel ──────────────────────────────────────────────────────────────

    @Test
    void cancel_deveCancelarComJustificativa() {
        User tec = tecnico(10L);
        Inspection insp = inspection(1L, InspectionStatus.SCHEDULED, tec);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tec));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        InspectionCancelRequest req = new InspectionCancelRequest();
        req.setReason("Cliente remarcou");

        InspectionResponse response = service.cancel(1L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.CANCELLED);
        assertThat(insp.getCancellationReason()).isEqualTo("Cliente remarcou");
        verify(auditLogService).log(eq(10L), anyString(), eq(AuditAction.CANCEL),
                eq("Inspection"), eq(1L), anyString());
    }

    @Test
    void cancel_deveFalharSeAprovada() {
        Inspection insp = inspection(1L, InspectionStatus.APPROVED, tecnico(10L));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionCancelRequest req = new InspectionCancelRequest();
        req.setReason("qualquer");

        assertThatThrownBy(() -> service.cancel(1L, req, 10L))
                .isInstanceOf(ConflictException.class);
        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void cancel_deveFalharSeJaCancelada() {
        Inspection insp = inspection(1L, InspectionStatus.CANCELLED, tecnico(10L));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionCancelRequest req = new InspectionCancelRequest();
        req.setReason("qualquer");

        assertThatThrownBy(() -> service.cancel(1L, req, 10L))
                .isInstanceOf(ConflictException.class);
    }

    // ── complete ──────────────────────────────────────────────────────────────

    @Test
    void complete_deveConcluirQuandoEmAndamento() {
        User tec = tecnico(10L);
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS, tec);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        InspectionCompleteRequest req = new InspectionCompleteRequest();
        req.setCompletedAtDevice(LocalDateTime.now());

        InspectionResponse response = service.complete(1L, req, 10L);

        assertThat(response).isNotNull();
        assertThat(insp.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
        assertThat(insp.getCompletedAtServer()).isNotNull();
    }

    @Test
    void complete_deveFalharSeNaoEmAndamento() {
        Inspection insp = inspection(1L, InspectionStatus.SCHEDULED, tecnico(10L));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionCompleteRequest req = new InspectionCompleteRequest();
        req.setCompletedAtDevice(LocalDateTime.now());

        assertThatThrownBy(() -> service.complete(1L, req, 10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void complete_deveFalharSeTecnicoNaoForOResponsavel() {
        Inspection insp = inspection(1L, InspectionStatus.IN_PROGRESS, tecnico(10L));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        InspectionCompleteRequest req = new InspectionCompleteRequest();
        req.setCompletedAtDevice(LocalDateTime.now());

        assertThatThrownBy(() -> service.complete(1L, req, 99L))
                .isInstanceOf(ConflictException.class);
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void create_deveCriarInspecaoComSnapshotGerado() {
        Client client = Client.builder().id(1L).name("Empresa ABC")
                .status(ClientStatus.ACTIVE).build();
        InspectionSite site = InspectionSite.builder().id(2L).name("Filial SP")
                .status(SiteStatus.ACTIVE).build();
        User tec = tecnico(10L);
        InspectionTemplateVersion version = InspectionTemplateVersion.builder()
                .id(3L).versionNumber(1).titleSnapshot("Template")
                .publishedAt(LocalDateTime.now()).activeForNewInspections(true).build();

        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(site));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tec));
        when(versionRepository.findById(3L)).thenReturn(Optional.of(version));
        when(inspectionRepository.save(any(Inspection.class)))
                .thenAnswer(inv -> { Inspection i = inv.getArgument(0); i.setId(1L); return i; });
        when(sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(3L)).thenReturn(List.of());
        when(snapshotRepository.findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(1L))
                .thenReturn(List.of());

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        InspectionResponse response = service.create(req);

        assertThat(response).isNotNull();
        assertThat(response.getClientId()).isEqualTo(1L);
        verify(auditLogService).log(isNull(), anyString(), eq(AuditAction.CREATE),
                eq("Inspection"), eq(1L), anyString());
    }

    @Test
    void create_deveFalharComVersaoNaoPublicada() {
        Client client = Client.builder().id(1L).name("ABC").status(ClientStatus.ACTIVE).build();
        InspectionSite site = InspectionSite.builder().id(2L).name("SP").status(SiteStatus.ACTIVE).build();
        User tec = tecnico(10L);
        InspectionTemplateVersion version = InspectionTemplateVersion.builder()
                .id(3L).versionNumber(1).titleSnapshot("T")
                .publishedAt(null).activeForNewInspections(true).build();

        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(site));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tec));
        when(versionRepository.findById(3L)).thenReturn(Optional.of(version));

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("publicada");
    }

    @Test
    void create_deveFalharComTecnicoInativo() {
        Client client = Client.builder().id(1L).name("ABC").status(ClientStatus.ACTIVE).build();
        InspectionSite site = InspectionSite.builder().id(2L).name("SP").status(SiteStatus.ACTIVE).build();
        User tecInativo = User.builder().id(10L).name("Carlos").email("t@f.com")
                .role(UserRole.TECNICO).active(false).build();

        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(site));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tecInativo));

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Técnico inativo");
    }

    @Test
    void create_deveFalharComEquipamentoDeOutroSite() {
        Client client = Client.builder().id(1L).name("ABC").status(ClientStatus.ACTIVE).build();
        InspectionSite site = InspectionSite.builder().id(2L).name("SP").status(SiteStatus.ACTIVE).build();
        InspectionSite outroSite = InspectionSite.builder().id(99L).name("RJ").status(SiteStatus.ACTIVE).build();
        User tec = tecnico(10L);
        InspectionTemplateVersion version = InspectionTemplateVersion.builder()
                .id(3L).versionNumber(1).titleSnapshot("T")
                .publishedAt(LocalDateTime.now()).activeForNewInspections(true).build();
        Equipment eq = Equipment.builder().id(7L).name("Compressor")
                .status(EquipmentStatus.ACTIVE).site(outroSite).build();

        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(site));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tec));
        when(versionRepository.findById(3L)).thenReturn(Optional.of(version));
        when(equipmentRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(eq));

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setEquipmentId(7L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pertence");
    }

    @Test
    void create_deveFalharComEquipamentoInativo() {
        Client client = Client.builder().id(1L).name("ABC").status(ClientStatus.ACTIVE).build();
        InspectionSite site = InspectionSite.builder().id(2L).name("SP").status(SiteStatus.ACTIVE).build();
        User tec = tecnico(10L);
        InspectionTemplateVersion version = InspectionTemplateVersion.builder()
                .id(3L).versionNumber(1).titleSnapshot("T")
                .publishedAt(LocalDateTime.now()).activeForNewInspections(true).build();
        Equipment eq = Equipment.builder().id(7L).name("Compressor")
                .status(EquipmentStatus.INACTIVE).site(site).build();

        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(site));
        when(userRepository.findById(10L)).thenReturn(Optional.of(tec));
        when(versionRepository.findById(3L)).thenReturn(Optional.of(version));
        when(equipmentRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(eq));

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setEquipmentId(7L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void create_deveFalharComClienteInativo() {
        Client client = Client.builder().id(1L).name("Empresa ABC")
                .status(ClientStatus.INACTIVE).build();
        when(clientRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(client));

        InspectionRequest req = new InspectionRequest();
        req.setClientId(1L);
        req.setSiteId(2L);
        req.setAssignedToId(10L);
        req.setTemplateVersionId(3L);
        req.setScheduledDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inativo");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_deveLancarNotFoundQuandoNaoExiste() {
        when(inspectionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

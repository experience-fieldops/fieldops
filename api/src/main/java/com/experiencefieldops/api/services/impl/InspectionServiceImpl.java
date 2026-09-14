package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionCancelRequest;
import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionItemSnapshotResponse;
import com.experiencefieldops.api.dto.InspectionRequest;
import com.experiencefieldops.api.dto.InspectionResponse;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.TemplateItem;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.enums.EquipmentStatus;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SiteStatus;
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
import com.experiencefieldops.api.services.InspectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;
    private final ClientRepository clientRepository;
    private final InspectionSiteRepository siteRepository;
    private final UserRepository userRepository;
    private final InspectionTemplateVersionRepository versionRepository;
    private final EquipmentRepository equipmentRepository;
    private final TemplateSectionRepository sectionRepository;
    private final TemplateItemRepository itemRepository;

    @Override
    @Transactional
    public InspectionResponse create(InspectionRequest request) {
        Client client = findActiveClient(request.getClientId());
        InspectionSite site = findActiveSite(request.getSiteId());
        User technician = findActiveTechnician(request.getAssignedToId());
        InspectionTemplateVersion templateVersion = findPublishedVersion(request.getTemplateVersionId());
        Equipment equipment = resolveEquipment(request.getEquipmentId());

        // Valida compatibilidade local ↔ equipamento (RN-014)
        if (equipment != null && !equipment.getSite().getId().equals(site.getId())) {
            throw new IllegalArgumentException(
                    "Equipamento não pertence ao local selecionado (RN-014)");
        }

        Inspection inspection = Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(technician)
                .templateVersion(templateVersion)
                .equipment(equipment)
                .scheduledDate(request.getScheduledDate())
                .status(InspectionStatus.SCHEDULED)
                .build();

        Inspection saved = inspectionRepository.save(inspection);

        // Gera snapshot de todos os itens da versão (RN-021)
        generateSnapshots(saved, templateVersion);

        List<InspectionItemSnapshotResponse> snapshots = loadSnapshots(saved.getId());
        return InspectionResponse.from(saved, snapshots);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InspectionResponse> findAll(Long technicianId, Long clientId,
            InspectionStatus status, Pageable pageable) {

        Specification<Inspection> spec = Specification
                .where(hasTechnician(technicianId))
                .and(hasClient(clientId))
                .and(hasStatus(status));

        return inspectionRepository.findAll(spec, pageable)
                .map(InspectionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionResponse findById(Long id) {
        Inspection inspection = findInspection(id);
        List<InspectionItemSnapshotResponse> snapshots = loadSnapshots(id);
        return InspectionResponse.from(inspection, snapshots);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InspectionResponse> findAssigned(Long technicianId, Pageable pageable) {
        Specification<Inspection> spec = Specification.where(hasTechnician(technicianId));
        return inspectionRepository.findAll(spec, pageable).map(InspectionResponse::from);
    }

    @Override
    @Transactional
    public InspectionResponse start(Long id, InspectionStartRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        // RN-032: somente SCHEDULED pode ser iniciada
        if (inspection.getStatus() != InspectionStatus.SCHEDULED) {
            throw new ConflictException("Somente inspeções agendadas podem ser iniciadas (RN-032). Status atual: "
                    + inspection.getStatus());
        }

        // RN-033: somente o técnico responsável pode iniciar
        if (!inspection.getAssignedTo().getId().equals(requestingUserId)) {
            throw new ConflictException("Somente o técnico responsável pode iniciar a inspeção (RN-033)");
        }

        inspection.setStatus(InspectionStatus.IN_PROGRESS);
        inspection.setStartedAtDevice(request.getStartedAtDevice());
        inspection.setStartedAtServer(LocalDateTime.now());

        return InspectionResponse.from(inspectionRepository.save(inspection));
    }

    @Override
    @Transactional
    public InspectionResponse complete(Long id, InspectionCompleteRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        // Somente IN_PROGRESS pode ser concluída
        if (inspection.getStatus() != InspectionStatus.IN_PROGRESS) {
            throw new ConflictException("Somente inspeções em andamento podem ser concluídas. Status atual: "
                    + inspection.getStatus());
        }

        // RN-033: somente o técnico responsável
        if (!inspection.getAssignedTo().getId().equals(requestingUserId)) {
            throw new ConflictException("Somente o técnico responsável pode concluir a inspeção (RN-033)");
        }

        inspection.setStatus(InspectionStatus.COMPLETED);
        inspection.setCompletedAtDevice(request.getCompletedAtDevice());
        inspection.setCompletedAtServer(LocalDateTime.now());

        return InspectionResponse.from(inspectionRepository.save(inspection));
    }

    @Override
    @Transactional
    public InspectionResponse cancel(Long id, InspectionCancelRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        // RN-030: inspeção aprovada não pode ser cancelada pelo fluxo comum
        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new ConflictException("Inspeção aprovada não pode ser cancelada pelo fluxo comum (RN-030)");
        }

        // Não faz sentido cancelar o que já está cancelado
        if (inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new ConflictException("Inspeção já está cancelada");
        }

        User canceller = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requestingUserId));

        inspection.setStatus(InspectionStatus.CANCELLED);
        inspection.setCancelledBy(canceller);
        inspection.setCancelledAt(LocalDateTime.now());
        inspection.setCancellationReason(request.getReason());

        return InspectionResponse.from(inspectionRepository.save(inspection));
    }

    // ── snapshot ──────────────────────────────────────────────────────────────

    private void generateSnapshots(Inspection inspection, InspectionTemplateVersion version) {
        List<TemplateSection> sections = sectionRepository
                .findByTemplateVersionIdOrderByDisplayOrder(version.getId());

        for (TemplateSection section : sections) {
            List<TemplateItem> items = itemRepository
                    .findBySectionIdOrderByDisplayOrder(section.getId());

            for (TemplateItem item : items) {
                InspectionItemSnapshot snapshot = InspectionItemSnapshot.builder()
                        .inspection(inspection)
                        .originalItem(item)
                        .code(item.getCode())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .responseType(item.getResponseType())
                        .required(item.getRequired())
                        .observationRequiredOnFailure(item.getObservationRequiredOnFailure())
                        .evidenceRequiredOnFailure(item.getEvidenceRequiredOnFailure())
                        .optionsJson(item.getOptionsJson())
                        .sectionTitle(section.getTitle())
                        .sectionDisplayOrder(section.getDisplayOrder())
                        .itemDisplayOrder(item.getDisplayOrder())
                        .build();
                snapshotRepository.save(snapshot);
            }
        }
    }

    private List<InspectionItemSnapshotResponse> loadSnapshots(Long inspectionId) {
        return snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(inspectionId)
                .stream()
                .map(InspectionItemSnapshotResponse::from)
                .collect(Collectors.toList());
    }

    // ── validações ────────────────────────────────────────────────────────────

    private Client findActiveClient(Long id) {
        Client client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        if (client.getStatus() == ClientStatus.INACTIVE) {
            throw new IllegalArgumentException("Cliente inativo não pode ser selecionado em novos agendamentos (RN-013)");
        }
        return client;
    }

    private InspectionSite findActiveSite(Long id) {
        InspectionSite site = siteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionSite", id));
        if (site.getStatus() == SiteStatus.INACTIVE) {
            throw new IllegalArgumentException("Local inativo não pode ser selecionado em novos agendamentos");
        }
        return site;
    }

    private User findActiveTechnician(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new IllegalArgumentException("Somente técnico ativo pode receber nova atribuição (RN-027)");
        }
        return user;
    }

    private InspectionTemplateVersion findPublishedVersion(Long id) {
        InspectionTemplateVersion version = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TemplateVersion", id));
        // Versão publicada = publishedAt preenchido e activeForNewInspections = true (RN-024)
        if (version.getPublishedAt() == null) {
            throw new IllegalArgumentException("A versão do modelo ainda não foi publicada (RN-024)");
        }
        if (Boolean.FALSE.equals(version.getActiveForNewInspections())) {
            throw new IllegalArgumentException("Esta versão do modelo não está disponível para novos agendamentos (RN-024)");
        }
        return version;
    }

    private Equipment resolveEquipment(Long equipmentId) {
        if (equipmentId == null) return null;
        Equipment equipment = equipmentRepository.findByIdAndDeletedAtIsNull(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));
        if (equipment.getStatus() == EquipmentStatus.INACTIVE
                || equipment.getStatus() == EquipmentStatus.DECOMMISSIONED) {
            throw new IllegalArgumentException("Equipamento inativo não pode ser usado em nova inspeção (RN-012)");
        }
        return equipment;
    }

    private Inspection findInspection(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", id));
    }

    // ── Specifications ────────────────────────────────────────────────────────

    private Specification<Inspection> hasTechnician(Long technicianId) {
        if (technicianId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), technicianId);
    }

    private Specification<Inspection> hasClient(Long clientId) {
        if (clientId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("client").get("id"), clientId);
    }

    private Specification<Inspection> hasStatus(InspectionStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}

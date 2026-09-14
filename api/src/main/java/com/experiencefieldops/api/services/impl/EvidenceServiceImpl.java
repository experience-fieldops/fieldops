package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.experiencefieldops.api.dto.EvidenceResponse;
import com.experiencefieldops.api.entities.Evidence;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionResponse;
import com.experiencefieldops.api.entities.NonConformity;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.EvidenceRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionResponseRepository;
import com.experiencefieldops.api.repositories.NonConformityRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.EvidenceService;
import com.experiencefieldops.api.services.StorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

    // Tipos de conteúdo aceitos (RN-046)
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/heic",
            "application/pdf"
    );

    private final EvidenceRepository evidenceRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionResponseRepository responseRepository;
    private final NonConformityRepository ncRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${storage.max-size-bytes:10485760}") // 10 MB default
    private long maxFileSizeBytes;

    @Override
    @Transactional
    public EvidenceResponse upload(Long inspectionId, Long responseId, Long nonConformityId,
            MultipartFile file, Long uploadedByUserId) {

        Inspection inspection = findActiveInspection(inspectionId);
        validateFile(file);

        InspectionResponse inspectionResponse = resolveResponse(inspectionId, responseId);
        NonConformity nonConformity = resolveNonConformity(nonConformityId);

        String fileKey = storageService.store(file);

        Evidence evidence = Evidence.builder()
                .inspection(inspection)
                .inspectionResponse(inspectionResponse)
                .nonConformity(nonConformity)
                .fileKey(fileKey)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .build();

        Evidence saved = evidenceRepository.save(evidence);
        return EvidenceResponse.from(saved, storageService.getUrl(fileKey));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceResponse> findAll(Long inspectionId) {
        findInspection(inspectionId);
        return evidenceRepository.findByInspectionIdAndDeletedAtIsNull(inspectionId)
                .stream()
                .map(e -> EvidenceResponse.from(e, storageService.getUrl(e.getFileKey())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceResponse findById(Long id) {
        Evidence evidence = evidenceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        return EvidenceResponse.from(evidence, storageService.getUrl(evidence.getFileKey()));
    }

    @Override
    @Transactional
    public void delete(Long id, Long deletedByUserId, String reason) {
        Evidence evidence = evidenceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", id));

        // RN-049: evidências de inspeção aprovada são somente leitura
        if (evidence.getInspection().getStatus() == InspectionStatus.APPROVED) {
            throw new ForbiddenException("Evidências de inspeção aprovada são somente leitura (RN-049)");
        }

        User deletedBy = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", deletedByUserId));

        // Soft delete auditado (RN-048) — não remove o arquivo do storage imediatamente
        evidence.setDeletedAt(LocalDateTime.now());
        evidence.setDeletedBy(deletedBy);
        evidence.setDeletionReason(reason);
        evidenceRepository.save(evidence);

        // Remove o arquivo do storage após soft delete
        storageService.delete(evidence.getFileKey());
    }

    // ── validações ────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio");
        }

        // RN-046: tipo de arquivo permitido
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Tipo de arquivo não suportado: " + contentType
                            + ". Permitidos: JPEG, PNG, WEBP, HEIC, PDF");
        }

        // RN-046: tamanho máximo
        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Arquivo excede o tamanho máximo permitido de "
                            + (maxFileSizeBytes / 1024 / 1024) + " MB");
        }
    }

    private Inspection findActiveInspection(Long inspectionId) {
        Inspection inspection = findInspection(inspectionId);
        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new ForbiddenException("Inspeção aprovada não aceita novas evidências (RN-049)");
        }
        if (inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new IllegalArgumentException("Inspeção cancelada não aceita evidências");
        }
        return inspection;
    }

    private Inspection findInspection(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", inspectionId));
    }

    private InspectionResponse resolveResponse(Long inspectionId, Long responseId) {
        if (responseId == null) return null;
        return responseRepository.findByInspectionIdAndId(inspectionId, responseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InspectionResponse " + responseId + " não pertence à inspeção " + inspectionId));
    }

    private NonConformity resolveNonConformity(Long ncId) {
        if (ncId == null) return null;
        return ncRepository.findById(ncId)
                .orElseThrow(() -> new ResourceNotFoundException("NonConformity", ncId));
    }
}

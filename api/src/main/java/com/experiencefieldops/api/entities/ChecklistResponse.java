package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Resposta de um item do checklist durante a execução da inspeção.
 * Armazena o valor respondido, observação, coordenadas GPS e timestamp da resposta.
 */
@Entity
@Table(name = "checklist_responses")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Vínculos ──────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    private InspectionExecution execution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_item_id", nullable = false)
    private InspectionItemSnapshot snapshotItem;

    // ── Resposta ──────────────────────────────────────────────────────────────
    @Column(name = "response_value", columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false)
    private Boolean notApplicable;

    // ── GPS no momento da resposta ─────────────────────────────────────────────
    @Column
    private Double gpsLatitude;

    @Column
    private Double gpsLongitude;

    // ── Timestamp da resposta (capturado no dispositivo) ────────────────────────
    @Column
    private LocalDateTime respondedAt;

    // ── Auditoria ─────────────────────────────────────────────────────────────
    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

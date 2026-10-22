package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.ExecutionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Registro da execução real de uma inspeção em campo.
 * Rastreia início, fim, status e coordenadas GPS do técnico.
 */
@Entity
@Table(name = "inspection_executions")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Vínculos ──────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    // ── Estado ─────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ExecutionStatus status;

    // ── GPS de início ─────────────────────────────────────────────────────────
    @Column
    private Double startGpsLatitude;

    @Column
    private Double startGpsLongitude;

    // ── GPS de conclusão ──────────────────────────────────────────────────────
    @Column
    private Double endGpsLatitude;

    @Column
    private Double endGpsLongitude;

    // ── Timestamps ─────────────────────────────────────────────────────────────
    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    // ── Observações gerais do técnico ──────────────────────────────────────────
    @Column(length = 1000)
    private String notes;

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

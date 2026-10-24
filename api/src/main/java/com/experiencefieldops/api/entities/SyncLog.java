package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.SyncLogStatus;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Registra o resultado consolidado de cada chamada ao endpoint POST /api/v1/sync.
 *
 * Diferente do SyncOutbox (que rastreia operações individuais), o SyncLog
 * armazena o resultado por lote — quantas operações foram sincronizadas,
 * quantas falharam e o status geral (SUCCESS / PARTIAL / FAILED).
 */
@Entity
@Table(name = "sync_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Técnico que enviou o lote. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    /** Status consolidado do lote. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SyncLogStatus status;

    /** Total de operações recebidas no lote. */
    @Column(nullable = false)
    private Integer totalOperations;

    /** Operações que foram processadas com êxito ou puladas (idempotência). */
    @Column(nullable = false)
    private Integer syncedCount;

    /** Operações que falharam. */
    @Column(nullable = false)
    private Integer failedCount;

    /** Momento em que o lote foi recebido e processado. */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;
}

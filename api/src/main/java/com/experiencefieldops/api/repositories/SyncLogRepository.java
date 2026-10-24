package com.experiencefieldops.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.SyncLog;
import com.experiencefieldops.api.enums.SyncLogStatus;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    List<SyncLog> findByTechnicianIdOrderByProcessedAtDesc(Long technicianId);

    List<SyncLog> findByTechnicianIdAndStatusOrderByProcessedAtDesc(Long technicianId, SyncLogStatus status);
}

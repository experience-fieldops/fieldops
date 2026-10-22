package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.ChecklistResponse;

public interface ChecklistResponseRepository extends JpaRepository<ChecklistResponse, Long> {

    List<ChecklistResponse> findByExecutionIdOrderByRespondedAtAsc(Long executionId);

    Optional<ChecklistResponse> findByExecutionIdAndSnapshotItemId(Long executionId, Long snapshotItemId);

    long countByExecutionId(Long executionId);
}

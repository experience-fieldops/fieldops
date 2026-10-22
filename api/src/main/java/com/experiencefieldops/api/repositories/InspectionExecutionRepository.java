package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.InspectionExecution;
import com.experiencefieldops.api.enums.ExecutionStatus;

public interface InspectionExecutionRepository extends JpaRepository<InspectionExecution, Long> {

    List<InspectionExecution> findByInspectionIdOrderByStartedAtDesc(Long inspectionId);

    Optional<InspectionExecution> findByInspectionIdAndStatus(Long inspectionId, ExecutionStatus status);

    boolean existsByInspectionIdAndStatus(Long inspectionId, ExecutionStatus status);

    List<InspectionExecution> findByTechnicianIdAndStatusOrderByStartedAtDesc(Long technicianId, ExecutionStatus status);
}

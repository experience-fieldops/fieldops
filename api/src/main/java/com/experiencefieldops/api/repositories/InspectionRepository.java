package com.experiencefieldops.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.experiencefieldops.api.entities.Inspection;

public interface InspectionRepository extends JpaRepository<Inspection, Long>, JpaSpecificationExecutor<Inspection> {

    /** Linhas [InspectionStatus, Long count] das inspeções atribuídas ao técnico (B-07). */
    @Query("select i.status, count(i) from Inspection i where i.assignedTo.id = :technicianId group by i.status")
    List<Object[]> countByStatusForTechnician(@Param("technicianId") Long technicianId);
}

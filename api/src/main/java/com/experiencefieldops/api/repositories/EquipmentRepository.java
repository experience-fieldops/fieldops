package com.experiencefieldops.api.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.experiencefieldops.api.entities.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByLocationId(Long locationId);
}

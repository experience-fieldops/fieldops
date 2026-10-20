package com.experiencefieldops.api.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.experiencefieldops.api.entities.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByClientId(Long clientId);
}

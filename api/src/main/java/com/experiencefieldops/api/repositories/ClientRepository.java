package com.experiencefieldops.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.experiencefieldops.api.entities.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {}

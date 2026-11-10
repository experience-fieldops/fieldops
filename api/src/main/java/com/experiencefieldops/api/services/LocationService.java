package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.experiencefieldops.api.audit.AuditLogger;
import com.experiencefieldops.api.dto.LocationDto;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.Location;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.repositories.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository repo;
    private final ClientRepository clientRepo;
    private final AuditLogger auditLogger;

    public LocationService(LocationRepository repo,
                           ClientRepository clientRepo,
                           AuditLogger auditLogger) {
        this.repo = repo;
        this.clientRepo = clientRepo;
        this.auditLogger = auditLogger;
    }

    public List<LocationDto> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public LocationDto getById(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public LocationDto save(Long clientId, Location l) {
        Client client = clientRepo.findById(clientId).orElse(null);
        l.setClient(client);
        l.setCreatedAt(LocalDateTime.now());
        l.setUpdatedAt(LocalDateTime.now());
        if (l.getActive() == null) l.setActive(true);
        LocationDto saved = toDto(repo.save(l));
        auditLogger.entityCreated("Location", saved.id(), "system");
        return saved;
    }

    public LocationDto update(Long id, Long clientId, Location l) {
        return repo.findById(id).map(existing -> {
            existing.setName(l.getName());
            existing.setAddress(l.getAddress());
            existing.setCity(l.getCity());
            existing.setState(l.getState());
            existing.setZipCode(l.getZipCode());
            existing.setActive(l.getActive() != null ? l.getActive() : existing.getActive());
            if (clientId != null) {
                clientRepo.findById(clientId).ifPresent(existing::setClient);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            LocationDto updated = toDto(repo.save(existing));
            auditLogger.entityUpdated("Location", updated.id(), "system");
            return updated;
        }).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
        auditLogger.entityDeleted("Location", id, "system");
    }

    private LocationDto toDto(Location l) {
        return new LocationDto(
            l.getId(), l.getName(), l.getAddress(), l.getCity(), l.getState(), l.getZipCode(),
            l.getClient() != null ? l.getClient().getId() : null,
            l.getClient() != null ? l.getClient().getName() : null,
            l.getActive(), l.getCreatedAt(), l.getUpdatedAt()
        );
    }
}

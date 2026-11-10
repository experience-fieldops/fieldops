package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.experiencefieldops.api.audit.AuditLogger;
import com.experiencefieldops.api.dto.ClientDto;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.repositories.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository repo;
    private final AuditLogger auditLogger;

    public ClientService(ClientRepository repo, AuditLogger auditLogger) {
        this.repo = repo;
        this.auditLogger = auditLogger;
    }

    public List<ClientDto> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public ClientDto getById(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public ClientDto save(Client c) {
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        if (c.getActive() == null) c.setActive(true);
        ClientDto saved = toDto(repo.save(c));
        auditLogger.entityCreated("Client", saved.id(), "system");
        return saved;
    }

    public ClientDto update(Long id, Client c) {
        return repo.findById(id).map(existing -> {
            existing.setName(c.getName());
            existing.setContactEmail(c.getContactEmail());
            existing.setPhone(c.getPhone());
            existing.setDocument(c.getDocument());
            existing.setActive(c.getActive() != null ? c.getActive() : existing.getActive());
            existing.setUpdatedAt(LocalDateTime.now());
            ClientDto updated = toDto(repo.save(existing));
            auditLogger.entityUpdated("Client", updated.id(), "system");
            return updated;
        }).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
        auditLogger.entityDeleted("Client", id, "system");
    }

    private ClientDto toDto(Client c) {
        return new ClientDto(c.getId(), c.getName(), c.getContactEmail(),
                c.getPhone(), c.getDocument(), c.getActive(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}

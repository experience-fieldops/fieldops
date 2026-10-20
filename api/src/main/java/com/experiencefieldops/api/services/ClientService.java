package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.experiencefieldops.api.dto.ClientDto;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.repositories.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository repo;

    public ClientService(ClientRepository repo) {
        this.repo = repo;
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
        return toDto(repo.save(c));
    }

    public ClientDto update(Long id, Client c) {
        return repo.findById(id).map(existing -> {
            existing.setName(c.getName());
            existing.setContactEmail(c.getContactEmail());
            existing.setPhone(c.getPhone());
            existing.setDocument(c.getDocument());
            existing.setActive(c.getActive() != null ? c.getActive() : existing.getActive());
            existing.setUpdatedAt(LocalDateTime.now());
            return toDto(repo.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private ClientDto toDto(Client c) {
        return new ClientDto(c.getId(), c.getName(), c.getContactEmail(),
                c.getPhone(), c.getDocument(), c.getActive(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}

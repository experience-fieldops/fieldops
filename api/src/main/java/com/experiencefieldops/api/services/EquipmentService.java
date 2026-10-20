package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.experiencefieldops.api.dto.EquipmentDto;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.Location;
import com.experiencefieldops.api.repositories.EquipmentRepository;
import com.experiencefieldops.api.repositories.LocationRepository;

@Service
public class EquipmentService {

    private final EquipmentRepository repo;
    private final LocationRepository locationRepo;

    public EquipmentService(EquipmentRepository repo, LocationRepository locationRepo) {
        this.repo = repo;
        this.locationRepo = locationRepo;
    }

    public List<EquipmentDto> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public EquipmentDto getById(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public EquipmentDto save(Long locationId, Equipment e) {
        Location loc = locationRepo.findById(locationId).orElse(null);
        e.setLocation(loc);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        if (e.getStatus() == null) e.setStatus("ATIVO");
        return toDto(repo.save(e));
    }

    public EquipmentDto update(Long id, Long locationId, Equipment e) {
        return repo.findById(id).map(existing -> {
            existing.setName(e.getName());
            existing.setSerialNumber(e.getSerialNumber());
            existing.setQrCode(e.getQrCode());
            existing.setCategory(e.getCategory());
            existing.setStatus(e.getStatus() != null ? e.getStatus() : existing.getStatus());
            if (locationId != null) {
                locationRepo.findById(locationId).ifPresent(existing::setLocation);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            return toDto(repo.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private EquipmentDto toDto(Equipment e) {
        Location loc = e.getLocation();
        String clientName = (loc != null && loc.getClient() != null) ? loc.getClient().getName() : null;
        return new EquipmentDto(
            e.getId(), e.getName(), e.getSerialNumber(), e.getQrCode(),
            e.getCategory(), e.getStatus(),
            loc != null ? loc.getId() : null,
            loc != null ? loc.getName() : null,
            clientName,
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}

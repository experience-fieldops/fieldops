package com.experiencefieldops.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.experiencefieldops.api.dto.EquipmentDto;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.services.EquipmentService;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipmentDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDto> getById(@PathVariable Long id) {
        EquipmentDto dto = service.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<EquipmentDto> create(@RequestBody Map<String, Object> body) {
        Equipment e = new Equipment();
        e.setName((String) body.get("name"));
        e.setSerialNumber((String) body.get("serialNumber"));
        e.setQrCode((String) body.get("qrCode"));
        e.setCategory((String) body.get("category"));
        e.setStatus((String) body.get("status"));
        Long locationId = body.get("locationId") != null ? Long.valueOf(body.get("locationId").toString()) : null;
        return ResponseEntity.ok(service.save(locationId, e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDto> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Equipment e = new Equipment();
        e.setName((String) body.get("name"));
        e.setSerialNumber((String) body.get("serialNumber"));
        e.setQrCode((String) body.get("qrCode"));
        e.setCategory((String) body.get("category"));
        e.setStatus((String) body.get("status"));
        Long locationId = body.get("locationId") != null ? Long.valueOf(body.get("locationId").toString()) : null;
        EquipmentDto updated = service.update(id, locationId, e);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.experiencefieldops.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.experiencefieldops.api.dto.LocationDto;
import com.experiencefieldops.api.entities.Location;
import com.experiencefieldops.api.services.LocationService;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocationDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getById(@PathVariable Long id) {
        LocationDto dto = service.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<LocationDto> create(@RequestBody Map<String, Object> body) {
        Location l = new Location();
        l.setName((String) body.get("name"));
        l.setAddress((String) body.get("address"));
        l.setCity((String) body.get("city"));
        l.setState((String) body.get("state"));
        l.setZipCode((String) body.get("zipCode"));
        Object activeObj = body.get("active");
        l.setActive(activeObj == null || (Boolean) activeObj);
        Long clientId = body.get("clientId") != null ? Long.valueOf(body.get("clientId").toString()) : null;
        return ResponseEntity.ok(service.save(clientId, l));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Location l = new Location();
        l.setName((String) body.get("name"));
        l.setAddress((String) body.get("address"));
        l.setCity((String) body.get("city"));
        l.setState((String) body.get("state"));
        l.setZipCode((String) body.get("zipCode"));
        Object activeObj = body.get("active");
        l.setActive(activeObj == null || (Boolean) activeObj);
        Long clientId = body.get("clientId") != null ? Long.valueOf(body.get("clientId").toString()) : null;
        LocationDto updated = service.update(id, clientId, l);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

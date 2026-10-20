package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

public record EquipmentDto(
    Long id,
    String name,
    String serialNumber,
    String qrCode,
    String category,
    String status,
    Long locationId,
    String locationName,
    String clientName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

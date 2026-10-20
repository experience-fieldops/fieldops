package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

public record LocationDto(
    Long id,
    String name,
    String address,
    String city,
    String state,
    String zipCode,
    Long clientId,
    String clientName,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

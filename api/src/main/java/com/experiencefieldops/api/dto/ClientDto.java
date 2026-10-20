package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

public record ClientDto(
    Long id,
    String name,
    String contactEmail,
    String phone,
    String document,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.entities.role.UserRole;

public record UserRequest(
    String name,
    String email,
    String passwordHash,
    UserRole role,
    Boolean active
) {}

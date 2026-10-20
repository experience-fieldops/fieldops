package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.entities.role.UserRole;

public record LoginResponse(
    String token,
    UserDto user
) {
    public record UserDto(Long id, String name, String email, UserRole role) {}
}

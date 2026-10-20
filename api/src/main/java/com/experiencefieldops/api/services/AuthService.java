package com.experiencefieldops.api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.experiencefieldops.api.dto.LoginRequest;
import com.experiencefieldops.api.dto.LoginResponse;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Senha incorreta");
        }

        if (!user.getActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        // Token gerado no servidor — substituir por JWT assinado quando a camada de segurança for finalizada
        String token = "session-" + user.getId() + "-" + user.getRole().name();

        return new LoginResponse(
            token,
            new LoginResponse.UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole())
        );
    }
}

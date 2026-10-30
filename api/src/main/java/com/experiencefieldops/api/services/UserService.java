package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.experiencefieldops.api.audit.AuditLogger;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User save(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // Nunca logar o passwordHash — issue #78
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        User saved = userRepository.save(user);
        // actorId "system" indica operação via API sem usuário autenticado identificado.
        // TODO: substituir por SecurityContextHolder.getContext() após JWT.
        auditLogger.entityCreated("User", saved.getId(), "system");
        return saved;
    }

    public User update(User user) {
        if (user.getId() == null || !userRepository.existsById(user.getId())) {
            return null;
        }
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing == null) return null;

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setActive(user.getActive());
        existing.setUpdatedAt(LocalDateTime.now());

        // Nunca logar o passwordHash — issue #78
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()
                && !user.getPasswordHash().startsWith("$2a$")) {
            existing.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        User updated = userRepository.save(existing);
        auditLogger.entityUpdated("User", updated.getId(), "system");
        return updated;
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
        auditLogger.entityDeleted("User", id, "system");
    }
}

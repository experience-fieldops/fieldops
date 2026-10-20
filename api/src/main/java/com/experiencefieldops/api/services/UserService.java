package com.experiencefieldops.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User save(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // Hash the password if provided in plain text
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
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

        // Only update password if a new one is provided
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()
                && !user.getPasswordHash().startsWith("$2a$")) {
            existing.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        return userRepository.save(existing);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}

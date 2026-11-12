package com.experiencefieldops.api.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Usuário autenticado vindo do JWT (B-08). Os ids de usuário em query string
 * (requestingUserId, reviewerId, technicianId) ficam opcionais e só são aceitos se baterem com o token.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Usuário não autenticado");
        }
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .orElseThrow(() -> new InsufficientAuthenticationException("Usuário do token não encontrado"));
    }

    /** Quem executa a ação: sempre o usuário do token. Id informado diferente → 403. */
    public Long actorId(Long informedId) {
        Long id = get().getId();
        if (informedId != null && !informedId.equals(id)) {
            throw new ForbiddenException("O id informado (" + informedId + ") não corresponde ao usuário autenticado");
        }
        return id;
    }

    /** Técnico consultado: padrão é o próprio usuário; só ADMIN/SUPERVISOR podem consultar outro. */
    public Long technicianId(Long informedId) {
        User user = get();
        if (informedId == null || informedId.equals(user.getId())) {
            return user.getId();
        }
        if (user.getRole() == UserRole.TECNICO) {
            throw new ForbiddenException("Técnico só pode consultar os próprios dados");
        }
        return informedId;
    }
}

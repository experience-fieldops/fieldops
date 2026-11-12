package com.experiencefieldops.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CurrentUserService currentUser;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(Long id, UserRole role) {
        String email = "u" + id + "@x.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
        when(userRepository.findByEmailAndDeletedAtIsNull(email))
                .thenReturn(Optional.of(User.builder().id(id).email(email).role(role).build()));
    }

    @Test
    void actorId_usaOTokenEAceitaIdIgualOuAusente() {
        loginAs(10L, UserRole.TECNICO);
        assertThat(currentUser.actorId(null)).isEqualTo(10L);
        assertThat(currentUser.actorId(10L)).isEqualTo(10L);
    }

    @Test
    void actorId_idDiferenteDoToken_403MesmoParaAdmin() {
        loginAs(1L, UserRole.ADMIN);
        assertThatThrownBy(() -> currentUser.actorId(10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void technicianId_tecnicoSoOProprio_adminQualquer() {
        loginAs(10L, UserRole.TECNICO);
        assertThat(currentUser.technicianId(null)).isEqualTo(10L);
        assertThatThrownBy(() -> currentUser.technicianId(11L)).isInstanceOf(ForbiddenException.class);

        loginAs(1L, UserRole.ADMIN);
        assertThat(currentUser.technicianId(11L)).isEqualTo(11L);
        assertThat(currentUser.technicianId(null)).isEqualTo(1L);
    }

    @Test
    void semAutenticacao_401() {
        assertThatThrownBy(() -> currentUser.actorId(null)).isInstanceOf(AuthenticationException.class);
    }
}

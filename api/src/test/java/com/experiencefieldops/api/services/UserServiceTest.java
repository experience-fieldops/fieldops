package com.experiencefieldops.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.experiencefieldops.api.dto.UserRequest;
import com.experiencefieldops.api.dto.UserResponse;
import com.experiencefieldops.api.dto.UserUpdateRequest;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User buildUser(Long id, String email) {
        return User.builder()
                .id(id)
                .name("Carlos Tecnico")
                .email(email)
                .passwordHash("hash")
                .role(UserRole.TECNICO)
                .active(true)
                .build();
    }

    // ── save ────────────────────────────────────────────────────────────────

    @Test
    void save_deveCriarUsuarioComSenhaHasheada() {
        UserRequest request = new UserRequest("Carlos Tecnico", "tecnico@fieldops.com",
                "senha123", UserRole.TECNICO);

        when(userRepository.existsByEmail("tecnico@fieldops.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.save(request);

        assertThat(response.getEmail()).isEqualTo("tecnico@fieldops.com");
        assertThat(response.getRole()).isEqualTo(UserRole.TECNICO);
        assertThat(response.getActive()).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash-bcrypt");
        assertThat(captor.getValue().getActive()).isTrue();
    }

    @Test
    void save_deveLancarConflictQuandoEmailJaExiste() {
        UserRequest request = new UserRequest("Carlos", "dup@fieldops.com",
                "senha123", UserRole.TECNICO);
        when(userRepository.existsByEmail("dup@fieldops.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("dup@fieldops.com");

        verify(userRepository, never()).save(any());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_deveRetornarUsuarioQuandoExiste() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(buildUser(1L, "tecnico@fieldops.com")));

        UserResponse response = userService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("tecnico@fieldops.com");
    }

    @Test
    void getById_deveLancarNotFoundQuandoNaoExiste() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    void update_deveAtualizarCamposInformados() {
        User existing = buildUser(1L, "antigo@fieldops.com");
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("novo@fieldops.com")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha")).thenReturn("novo-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest("Novo Nome", "novo@fieldops.com",
                "novaSenha", UserRole.SUPERVISOR, false);

        UserResponse response = userService.update(1L, request);

        assertThat(response.getName()).isEqualTo("Novo Nome");
        assertThat(response.getEmail()).isEqualTo("novo@fieldops.com");
        assertThat(response.getRole()).isEqualTo(UserRole.SUPERVISOR);
        assertThat(response.getActive()).isFalse();
        assertThat(existing.getPasswordHash()).isEqualTo("novo-hash");
    }

    @Test
    void update_deveLancarConflictQuandoNovoEmailJaExiste() {
        User existing = buildUser(1L, "antigo@fieldops.com");
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("ocupado@fieldops.com")).thenReturn(true);

        UserUpdateRequest request = new UserUpdateRequest(null, "ocupado@fieldops.com",
                null, null, null);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_naoDeveReprocessarSenhaQuandoNaoInformada() {
        User existing = buildUser(1L, "antigo@fieldops.com");
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest("Só o nome", null, null, null, null);

        userService.update(1L, request);

        verify(passwordEncoder, never()).encode(anyString());
        assertThat(existing.getName()).isEqualTo("Só o nome");
    }

    // ── deleteById (soft delete) ──────────────────────────────────────────────

    @Test
    void deleteById_deveFazerSoftDelete() {
        User existing = buildUser(1L, "tecnico@fieldops.com");
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.deleteById(1L);

        assertThat(existing.getDeletedAt()).isNotNull();
        assertThat(existing.getActive()).isFalse();
        verify(userRepository).save(existing);
    }

    @Test
    void deleteById_deveLancarNotFoundQuandoNaoExiste() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_deveFiltrarUsuariosDeletados() {
        User ativo = buildUser(1L, "ativo@fieldops.com");
        User deletado = buildUser(2L, "deletado@fieldops.com");
        deletado.setDeletedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(List.of(ativo, deletado));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("ativo@fieldops.com");
    }
}

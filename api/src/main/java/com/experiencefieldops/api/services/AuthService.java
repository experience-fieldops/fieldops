package com.experiencefieldops.api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.experiencefieldops.api.audit.AuditLogger;
import com.experiencefieldops.api.dto.LoginRequest;
import com.experiencefieldops.api.dto.LoginResponse;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    /**
     * Autentica o usuário e retorna um token de sessão.
     *
     * Registra auditoria de login (sucesso e falha) com IP e e-mail mascarado.
     * O e-mail e a senha nunca são logados em texto plano — issue #78.
     *
     * @param request dados de login (email + password)
     * @param clientIp IP de origem da requisição (resolvido pelo controller)
     */
    public LoginResponse login(LoginRequest request, String clientIp) {

        // Busca o usuário — falha auditada antes de lançar exceção
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            auditLogger.loginFailure(request.email(), clientIp, "usuario-nao-encontrado");
            throw new RuntimeException("Usuário não encontrado");
        }

        // Valida a senha — nunca logamos a senha em nenhuma situação
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditLogger.loginFailure(request.email(), clientIp, "senha-incorreta");
            throw new RuntimeException("Senha incorreta");
        }

        // Valida se o usuário está ativo
        if (!user.getActive()) {
            auditLogger.loginFailure(request.email(), clientIp, "usuario-inativo");
            throw new RuntimeException("Usuário inativo");
        }

        // Token gerado no servidor — substituir por JWT assinado quando a camada de segurança for finalizada
        // ATENÇÃO: nunca logar o token completo — issue #78
        String token = "session-" + user.getId() + "-" + user.getRole().name();

        auditLogger.loginSuccess(request.email(), clientIp);

        return new LoginResponse(
            token,
            new LoginResponse.UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole())
        );
    }
}

package com.experiencefieldops.api.audit;

import com.experiencefieldops.api.dto.LoginRequest;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.entities.role.UserRole;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.security.AccessDeniedAuditHandler;
import com.experiencefieldops.api.services.AuthService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class SecurityAuditTest {

    @Test
    void loginSuccessAndFailureAreAuditedWithoutExposingCredentials(CapturedOutput output) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuditLogger auditLogger = new AuditLogger();
        AuthService authService = new AuthService(userRepository, passwordEncoder, auditLogger);
        User user = new User();
        user.setId(42L);
        user.setName("Field Operator");
        user.setEmail("operator@example.test");
        user.setPasswordHash("stored-password-hash");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plaintext-secret", user.getPasswordHash())).thenReturn(true);
        authService.login(new LoginRequest(user.getEmail(), "plaintext-secret"), "198.51.100.50");
        when(passwordEncoder.matches("wrong-plaintext-secret", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(user.getEmail(), "wrong-plaintext-secret"), "198.51.100.51"))
                .isInstanceOf(RuntimeException.class);

        assertThat(output).contains("LOGIN_SUCCESS", "LOGIN_FAILURE", "op***@example.test");
        assertThat(output).doesNotContain("operator@example.test", "plaintext-secret",
                "wrong-plaintext-secret", "stored-password-hash", "session-42-ADMIN");
    }

    @Test
    void accessDeniedHandlerReturns403AndWritesAnAuditEvent(CapturedOutput output) throws Exception {
        AccessDeniedAuditHandler handler = new AccessDeniedAuditHandler(new AuditLogger());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        request.setRemoteAddr("198.51.100.52");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(output).contains("DENIED", "GET /api/protected", "userId=anonymous");
    }

    @Test
    void requestMdcIncludesCorrelationAndIpThenClearsAfterTheRequest() throws Exception {
        MdcRequestFilter filter = new MdcRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user");
        request.setRemoteAddr("198.51.100.53");
        AtomicReference<String> requestId = new AtomicReference<>();
        AtomicReference<String> clientIp = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            requestId.set(MDC.get("requestId"));
            clientIp.set(MDC.get("clientIp"));
            assertThat(MDC.get("userId")).isNull();
        };
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(requestId.get()).isNotBlank();
        assertThat(clientIp.get()).isEqualTo("198.51.100.53");
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("clientIp")).isNull();
    }

    @Test
    void existingCrudAndOfflineAuditMethodsEmitEvents(CapturedOutput output) {
        AuditLogger auditLogger = new AuditLogger();
        auditLogger.entityCreated("Client", 1L, "system");
        auditLogger.entityUpdated("Equipment", 2L, "system");
        auditLogger.entityDeleted("Location", 3L, "system");
        auditLogger.syncOffline("user-4", "SUCCESS", 5);

        assertThat(output).contains("CREATED entity=Client", "UPDATED entity=Equipment",
                "DELETED entity=Location", "OFFLINE userId=user-4 result=SUCCESS items=5");
    }
}

package com.experiencefieldops.api.security;

import com.experiencefieldops.api.audit.AuditLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler de acesso negado (403) com auditoria de segurança (issue #78).
 *
 * Registra no log:
 *   - Identificador do usuário (ou "anonymous" se não autenticado)
 *   - Endpoint acessado (método + URI)
 *   - IP de origem
 *
 * Retorna resposta JSON padronizada 403 para o cliente.
 *
 * Registrado no SecurityConfig via .exceptionHandling().accessDeniedHandler(...)
 */
@Component
public class AccessDeniedAuditHandler implements AccessDeniedHandler {

    private final AuditLogger auditLogger;

    public AccessDeniedAuditHandler(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String userId = resolveUserId(request);
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        String clientIp = resolveClientIp(request);

        auditLogger.accessDenied(userId, endpoint, clientIp);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {
                  "status": 403,
                  "error": "Forbidden",
                  "message": "Acesso negado. Você não tem permissão para acessar este recurso."
                }
                """);
    }

    /**
     * Tenta extrair o ID do usuário a partir do header Authorization.
     * Enquanto JWT não estiver implementado, faz parse do token de sessão.
     * Retorna "anonymous" se não houver autenticação.
     *
     * TODO: após JWT, usar SecurityContextHolder para extrair o subject.
     */
    private String resolveUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("session-")) {
            // Formato atual: "session-{userId}-{ROLE}"
            String[] parts = auth.split("-");
            if (parts.length >= 2) return "userId:" + parts[1];
        }
        return "anonymous";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

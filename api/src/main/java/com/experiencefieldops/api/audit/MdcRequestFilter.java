package com.experiencefieldops.api.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que popula o MDC (Mapped Diagnostic Context) do SLF4J (issue #78).
 *
 * Campos adicionados ao contexto de cada requisição:
 *   requestId → UUID único por requisição (útil para correlacionar logs no ELK)
 *   clientIp  → IP real do cliente (respeitando X-Forwarded-For)
 *
 * Esses campos aparecem automaticamente em TODOS os logs emitidos
 * durante o processamento da requisição, incluindo os logs de auditoria.
 *
 * No perfil prod (JSON), o LogstashEncoder inclui os campos MDC no objeto JSON:
 *   { "@timestamp": "...", "requestId": "abc-123", "clientIp": "1.2.3.4", ... }
 *
 * O MDC é sempre limpo no bloco finally para evitar vazamento entre requisições
 * em ambientes com thread pool (ex: Tomcat).
 *
 * Nota: userId não é populado aqui pois depende de JWT (ainda não implementado).
 * TODO: após JWT, extrair o subject do token e popular MDC.put("userId", subject).
 */
@Component
@Order(1) // Executar antes de qualquer outro filtro para que o MDC esteja disponível
public class MdcRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // requestId único por requisição — facilita correlação de logs no ELK/Grafana
            MDC.put("requestId", UUID.randomUUID().toString());

            // IP real do cliente
            MDC.put("clientIp", resolveClientIp(request));

            filterChain.doFilter(request, response);

        } finally {
            // SEMPRE limpar o MDC para evitar vazamento entre threads reutilizadas
            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

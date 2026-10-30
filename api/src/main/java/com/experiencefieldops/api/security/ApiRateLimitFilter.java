package com.experiencefieldops.api.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting geral para todos os endpoints da API (issue #77).
 *
 * Estratégia: Token Bucket por token de autorização.
 * Enquanto JWT não estiver implementado, o fallback é por IP.
 *
 * Limites:
 *   - 100 requisições por minuto por token (ou IP quando sem token)
 *
 * Ao exceder o limite:
 *   - Responde 429 Too Many Requests
 *   - Registra log de auditoria
 *
 * Nota: endpoints públicos (/api/public/**) não são afetados por este filtro
 * — o LoginRateLimitFilter já cobre o /login com limites mais restritivos.
 *
 * TODO: quando JWT for implementado, extrair o subject do token para usar
 * como chave em vez do raw header Authorization.
 */
@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimitFilter.class);

    private static final int    MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration REFILL_DURATION       = Duration.ofMinutes(1);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Endpoints públicos têm tratamento próprio (LoginRateLimitFilter)
        if (request.getRequestURI().startsWith("/api/public/")
                || request.getRequestURI().startsWith("/h2-console/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = resolveBucketKey(request);
        Bucket bucket = buckets.computeIfAbsent(bucketKey, this::createBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("[RATE-LIMIT] Limite da API excedido — chave: {} | Path: {} | Limite: {}/{} por {}",
                    bucketKey, request.getRequestURI(),
                    MAX_REQUESTS_PER_MINUTE, MAX_REQUESTS_PER_MINUTE, REFILL_DURATION);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Limite de requisições excedido. Tente novamente em instantes."
                    }
                    """);
        }
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                MAX_REQUESTS_PER_MINUTE,
                Refill.intervally(MAX_REQUESTS_PER_MINUTE, REFILL_DURATION));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Determina a chave do bucket:
     *   1. Header Authorization presente → usa o token (identificador do cliente autenticado)
     *   2. Sem Authorization           → fallback por IP
     *
     * TODO: após implementação de JWT, extrair o claim "sub" do token.
     */
    private String resolveBucketKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            return "token:" + authHeader;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip:" + forwarded.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}

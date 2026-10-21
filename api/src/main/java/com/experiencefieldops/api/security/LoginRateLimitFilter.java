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
 * Filtro de rate limiting para o endpoint de autenticação (issue #77).
 *
 * Estratégia: Token Bucket por IP de origem.
 *
 * Limites:
 *   - POST /api/public/auth/login → máximo 5 tentativas por minuto por IP
 *
 * Ao exceder o limite:
 *   - Responde 429 Too Many Requests com mensagem JSON
 *   - Registra log de auditoria com IP e timestamp
 *
 * Algoritmo escolhido (Token Bucket):
 *   Cada IP recebe um "balde" com 5 tokens que se reabastece completamente
 *   a cada 60 segundos. Cada requisição consome 1 token. Quando o balde
 *   esvazia, a requisição é bloqueada até o próximo reabastecimento.
 *
 * Armazenamento dos buckets:
 *   ConcurrentHashMap em memória — adequado para instância única.
 *   Para múltiplas instâncias (cluster), substituir por Bucket4j + Redis/Hazelcast.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);

    /** Endpoint protegido */
    private static final String LOGIN_PATH = "/api/public/auth/login";

    /** Máximo de tentativas por janela de tempo */
    private static final int MAX_ATTEMPTS = 5;

    /** Janela de tempo para reabastecimento dos tokens */
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    /**
     * Mapa de buckets por IP.
     * Uso de ConcurrentHashMap garante thread-safety sem sincronização explícita.
     */
    private final Map<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Aplica rate limit apenas no endpoint de login via POST
        if (!isLoginEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = bucketsByIp.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            // Tokens disponíveis — permite a requisição
            filterChain.doFilter(request, response);
        } else {
            // Limite excedido — bloqueia e registra para auditoria
            log.warn("[RATE-LIMIT] Tentativa de login bloqueada — IP: {} | Path: {} | Limite: {}/{} por {}",
                    clientIp, LOGIN_PATH, MAX_ATTEMPTS, MAX_ATTEMPTS, REFILL_DURATION);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Muitas tentativas de login. Aguarde 1 minuto e tente novamente."
                    }
                    """);
        }
    }

    /**
     * Cria um novo bucket para o IP informado.
     * Capacidade: MAX_ATTEMPTS tokens; reabastecimento completo a cada REFILL_DURATION.
     */
    private Bucket createBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
                MAX_ATTEMPTS,
                Refill.intervally(MAX_ATTEMPTS, REFILL_DURATION));
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isLoginEndpoint(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());
    }

    /**
     * Resolve o IP real do cliente, respeitando proxies reversos.
     * Prioriza o header X-Forwarded-For quando presente (ex: nginx, AWS ALB).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For pode conter uma cadeia: "clientIp, proxy1, proxy2"
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

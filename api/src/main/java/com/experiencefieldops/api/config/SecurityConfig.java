package com.experiencefieldops.api.config;

import com.experiencefieldops.api.security.ApiRateLimitFilter;
import com.experiencefieldops.api.security.LoginRateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração central de segurança HTTP.
 *
 * Cobre:
 *  - Headers de segurança HTTP (issue #76)
 *  - Rate limiting no login e na API geral (issue #77)
 *  - CORS
 *  - Autorização de endpoints
 *
 * Como validar os headers em staging:
 *  1. Suba a aplicação e acesse a URL pública.
 *  2. Acesse https://securityheaders.com e informe a URL.
 *  3. Espera-se nota A ou A+.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final LoginRateLimitFilter loginRateLimitFilter;
    private final ApiRateLimitFilter apiRateLimitFilter;

    public SecurityConfig(LoginRateLimitFilter loginRateLimitFilter,
                          ApiRateLimitFilter apiRateLimitFilter) {
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.apiRateLimitFilter = apiRateLimitFilter;
    }

    /**
     * Cadeia exclusiva para o H2 Console (apenas ambiente de desenvolvimento).
     *
     * Precisa de @Order menor que a cadeia principal para ter prioridade.
     * Permite iframes (frameOptions = SAMEORIGIN) necessários para o console H2.
     * Em produção, desabilite o H2 Console via application.properties:
     *   spring.h2.console.enabled=false
     */
    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/h2-console/**")
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * Cadeia principal de filtros de segurança.
     *
     * Ordem dos filtros de rate limiting (issue #77):
     *   1. LoginRateLimitFilter  → 5 req/min por IP em POST /api/public/auth/login
     *   2. ApiRateLimitFilter    → 100 req/min por token (ou IP) nos demais endpoints
     *
     * Headers configurados (issue #76):
     * ┌─────────────────────────────────────┬──────────────────────────────────────────────────┐
     * │ Header                              │ Valor                                            │
     * ├─────────────────────────────────────┼──────────────────────────────────────────────────┤
     * │ X-Content-Type-Options              │ nosniff                                          │
     * │ X-Frame-Options                     │ DENY                                             │
     * │ X-XSS-Protection                    │ 1; mode=block                                    │
     * │ Strict-Transport-Security (HSTS)    │ max-age=31536000; includeSubDomains              │
     * │ Content-Security-Policy (CSP)       │ default-src 'self'; restrito para produção       │
     * │ Referrer-Policy                     │ strict-origin-when-cross-origin                  │
     * │ Permissions-Policy                  │ câmera, microfone e geolocalização desabilitados │
     * └─────────────────────────────────────┴──────────────────────────────────────────────────┘
     *
     * Headers removidos:
     *  - Server → suprimido via application.properties (server.server-header=)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF desabilitado porque a API é stateless (tokens via header).
            // Reavaliar quando sessões com cookie forem introduzidas.
            .csrf(AbstractHttpConfigurer::disable)

            // ── Rate limiting (issue #77) ───────────────────────────────────────
            // Os filtros são adicionados antes do filtro de autenticação padrão do Spring Security.
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiRateLimitFilter,   UsernamePasswordAuthenticationFilter.class)

            // ── Headers de segurança HTTP (issue #76) ──────────────────────────
            .headers(headers -> headers

                // Impede MIME-type sniffing pelo navegador
                .contentTypeOptions(contentType -> { /* nosniff — habilitado por padrão */ })

                // Bloqueia carregamento da página em iframe de origens externas
                .frameOptions(frame -> frame.deny())

                // Proteção XSS legada (ainda útil para navegadores antigos)
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))

                // HSTS: força HTTPS por 1 ano, incluindo subdomínios.
                // Ative somente quando TLS estiver configurado em produção.
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31_536_000)
                    .includeSubDomains(true))

                // CSP: restringe de onde recursos podem ser carregados.
                // Ajuste as diretivas conforme os domínios de CDN/API usados em produção.
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +   // unsafe-inline necessário enquanto não houver hashes de estilo
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +             // equivalente a X-Frame-Options: DENY
                        "form-action 'self'"))

                // Não vaza a URL de referência para domínios externos
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                // Permissions-Policy: desabilita APIs sensíveis do navegador
                .permissionsPolicy(permissions -> permissions
                    .policy("camera=(), microphone=(), geolocation=()")))

            // ── Autorização de endpoints ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (login, etc.)
                .requestMatchers("/api/public/**").permitAll()
                // TODO: substituir por .authenticated() após implementar JWT
                .anyRequest().permitAll());

        return http.build();
    }

    /**
     * CORS: permite apenas as origens conhecidas do frontend.
     * Adicione a URL de produção aqui quando disponível.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

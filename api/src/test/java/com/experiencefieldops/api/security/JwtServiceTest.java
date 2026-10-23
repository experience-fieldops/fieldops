package com.experiencefieldops.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.List;

import io.jsonwebtoken.ExpiredJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    // Chave Base64 com 256 bits (32 bytes) — exigida pelo HS256.
    private static final String SECRET =
            Base64.getEncoder().encodeToString("0123456789-0123456789-0123456789".getBytes());

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L); // 1h

        userDetails = User.builder()
                .username("tecnico@fieldops.com")
                .password("irrelevante")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_TECNICO")))
                .build();
    }

    @Test
    void generateToken_deveProduzirTokenNaoVazio() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        // JWT tem 3 segmentos separados por ponto.
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_deveRetornarOSubjectDoToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("tecnico@fieldops.com");
    }

    @Test
    void isTokenValid_deveSerTrueParaOMesmoUsuario() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_deveSerFalseParaUsuarioDiferente() {
        String token = jwtService.generateToken(userDetails);

        UserDetails outro = User.builder()
                .username("outro@fieldops.com")
                .password("irrelevante")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_TECNICO")))
                .build();

        assertThat(jwtService.isTokenValid(token, outro)).isFalse();
    }

    @Test
    void isTokenValid_deveRejeitarTokenExpirado() {
        // expiração negativa → token já nasce expirado.
        // A jjwt lança ExpiredJwtException ao fazer o parse de um token expirado.
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String tokenExpirado = jwtService.generateToken(userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(tokenExpirado, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void getExpiration_deveRefletirOValorConfigurado() {
        assertThat(jwtService.getExpiration()).isEqualTo(3_600_000L);
    }
}

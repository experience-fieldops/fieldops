package com.experiencefieldops.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.LoginRequest;
import com.experiencefieldops.api.dto.LoginResponse;
import com.experiencefieldops.api.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            // Resolve o IP real respeitando proxies reversos (nginx, AWS ALB)
            String clientIp = resolveClientIp(httpRequest);
            LoginResponse response = authService.login(request, clientIp);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // A auditoria de falha já foi registrada no AuthService
            return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas"));
        }
    }

    /** Extrai o IP real do cliente, respeitando a cadeia X-Forwarded-For */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

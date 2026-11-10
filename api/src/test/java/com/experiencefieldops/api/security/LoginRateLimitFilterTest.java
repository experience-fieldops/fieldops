package com.experiencefieldops.api.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LoginRateLimitFilterTest {

    @Test
    void loginAllowsFiveAttemptsThenReturns429AndLogsTheBlockedIp(CapturedOutput output)
            throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();
        AtomicInteger passedRequests = new AtomicInteger();

        for (int attempt = 1; attempt <= 6; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest(
                    "POST", "/api/public/auth/login");
            request.setRemoteAddr("198.51.100.42");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> passedRequests.incrementAndGet();

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(attempt <= 5 ? 200 : 429);
        }

        assertThat(passedRequests).hasValue(5);
        assertThat(output).contains("Tentativa de login bloqueada", "198.51.100.42");
    }

    @Test
    void forwardedClientIpIsUsedAsTheLoginBucketKey() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        for (int attempt = 0; attempt < 5; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest(
                    "POST", "/api/public/auth/login");
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Forwarded-For", "198.51.100.43, 10.0.0.2");
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });
        }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest(
                "POST", "/api/public/auth/login");
        blockedRequest.setRemoteAddr("10.0.0.1");
        blockedRequest.addHeader("X-Forwarded-For", "198.51.100.43, 10.0.0.3");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(blockedRequest, blockedResponse, (req, res) -> { });

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
    }
}

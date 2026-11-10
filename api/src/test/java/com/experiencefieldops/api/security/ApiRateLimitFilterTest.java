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
class ApiRateLimitFilterTest {

    @Test
    void generalLimitIsOneHundredRequestsPerMinutePerIpRegardlessOfAuthorizationHeader(
            CapturedOutput output) throws Exception {
        ApiRateLimitFilter filter = new ApiRateLimitFilter();
        AtomicInteger passedRequests = new AtomicInteger();

        for (int attempt = 1; attempt <= 101; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user");
            request.setRemoteAddr("198.51.100.40");
            request.addHeader("Authorization", "Bearer sensitive-token-" + attempt);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> passedRequests.incrementAndGet();

            filter.doFilter(request, response, chain);

            if (attempt <= 100) {
                assertThat(response.getStatus()).isEqualTo(200);
            } else {
                assertThat(response.getStatus()).isEqualTo(429);
            }
        }

        assertThat(passedRequests).hasValue(100);
        assertThat(output).contains("198.51.100.40");
        assertThat(output).doesNotContain("sensitive-token");
    }

    @Test
    void publicEndpointsAreNotCountedByTheGeneralApiLimit() throws Exception {
        ApiRateLimitFilter filter = new ApiRateLimitFilter();
        AtomicInteger passedRequests = new AtomicInteger();

        for (int attempt = 0; attempt < 101; attempt++) {
            MockHttpServletRequest request = new MockHttpServletRequest(
                    "POST", "/api/public/auth/login");
            request.setRemoteAddr("198.51.100.41");
            filter.doFilter(request, new MockHttpServletResponse(),
                    (req, res) -> passedRequests.incrementAndGet());
        }

        assertThat(passedRequests).hasValue(101);
    }
}

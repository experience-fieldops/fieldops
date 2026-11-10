package com.experiencefieldops.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.GET;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityHttpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void apiResponsesIncludeSecurityHeadersAndDoNotExposeServerHeaders() throws Exception {
        mockMvc.perform(get("/api/user").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().string("Strict-Transport-Security",
                        "max-age=31536000 ; includeSubDomains"))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("default-src 'self'")))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=()"))
                .andExpect(header().doesNotExist("X-Powered-By"))
                .andExpect(header().doesNotExist("Server"));
    }

    @Test
    void embeddedServerDoesNotExposeServerOrPoweredByHeaders() {
        var response = restTemplate.exchange("/api/user", GET, EMPTY, String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getFirst(HttpHeaders.SERVER)).isNull();
        assertThat(response.getHeaders().getFirst("X-Powered-By")).isNull();
    }

    @Test
    void uploadLimitsAndDirectoryAreConfiguredInTheSingleDefaultPropertiesFile() throws IOException {
        assertThat(environment.getProperty("spring.servlet.multipart.max-file-size")).isEqualTo("10MB");
        assertThat(environment.getProperty("spring.servlet.multipart.max-request-size")).isEqualTo("10MB");
        assertThat(Path.of(environment.getRequiredProperty("upload.directory")).isAbsolute()).isTrue();
        assertThat(environment.getProperty("server.server-header")).isEmpty();
        Resource[] configFiles = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:application*.properties");
        assertThat(Arrays.stream(configFiles).map(Resource::getFilename))
                .containsExactly("application.properties");
    }
}

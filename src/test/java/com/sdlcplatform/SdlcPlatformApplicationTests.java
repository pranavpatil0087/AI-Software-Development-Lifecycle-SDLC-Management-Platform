package com.sdlcplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Boots the full Spring context against a real, ephemeral PostgreSQL
 * container so we know the application, Flyway migrations, and security
 * configuration all wire together correctly — not just that classes compile.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SdlcPlatformApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sdlc_platform_test")
            .withUsername("sdlc_user")
            .withPassword("sdlc_password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        // If the Spring context fails to start (bad bean wiring, broken
        // migrations, misconfigured security), this test fails.
    }
}

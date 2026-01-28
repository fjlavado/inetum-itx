package com.inetum.prices.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests using Testcontainers.
 * <p>
 * This class provides a shared PostgreSQL container for all integration tests,
 * significantly improving test execution performance by reusing the same container.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Starts a real PostgreSQL 16 container via Docker</li>
 *   <li>Container is shared across all tests (static singleton pattern)</li>
 *   <li>Flyway migrations run automatically against the containerized database</li>
 *   <li>Spring Boot context is fully loaded with real dependencies</li>
 *   <li>Tests run on a random port to avoid conflicts</li>
 *   <li>Uses 'test' profile with cache disabled for accurate testing</li>
 * </ul>
 * <p>
 * <b>Usage:</b> Extend this class in your integration tests:
 * <pre>
 * {@code
 * @DisplayName("Price Controller Integration Tests")
 * class PriceControllerIntegrationTest extends AbstractIntegrationTest {
 *     // Your tests here
 * }
 * }
 * </pre>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.defer-datasource-initialization=false"
    }
)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container configuration.
     * <p>
     * Static container instance is reused across all tests for performance.
     * Container uses PostgreSQL 16 Alpine (lightweight image).
     */
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Dynamically configures Spring Boot properties to point to the Testcontainer.
     * <p>
     * This method overrides the default application.yml datasource configuration
     * with the dynamically assigned container host, port, and credentials.
     *
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // Ensure Flyway uses the same datasource
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
    }
}

package com.inetum.prices.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for reactive integration tests using Testcontainers.
 * <p>
 * This class provides a shared PostgreSQL container for all integration tests,
 * configured for both R2DBC (reactive access) and JDBC (Flyway migrations).
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Starts a real PostgreSQL 16 container via Docker</li>
 *   <li>Container is shared across all tests (static singleton pattern)</li>
 *   <li>R2DBC configured for reactive database access</li>
 *   <li>JDBC configured for Flyway migrations (Flyway doesn't support R2DBC)</li>
 *   <li>Flyway migrations run automatically against the containerized database</li>
 *   <li>Spring Boot WebFlux context with WebTestClient support</li>
 *   <li>Tests run on a random port to avoid conflicts</li>
 *   <li>Uses 'test' profile with cache disabled for accurate testing</li>
 * </ul>
 * <p>
 * <b>Usage:</b> Extend this class in your integration tests:
 * <pre>
 * {@code
 * @DisplayName("Price Controller Integration Tests")
 * class PriceControllerIntegrationTest extends AbstractIntegrationTest {
 *     @Autowired
 *     private WebTestClient webTestClient;
 *     // Your tests here
 * }
 * }
 * </pre>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
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
     * This method configures both R2DBC (for reactive database access) and JDBC
     * (for Flyway migrations) to use the same Testcontainer instance.
     * <p>
     * <b>R2DBC URL Format:</b> r2dbc:postgresql://host:port/database
     * <br>
     * <b>JDBC URL Format:</b> jdbc:postgresql://host:port/database
     *
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // R2DBC configuration for reactive database access
        String r2dbcUrl = String.format(
                "r2dbc:postgresql://%s:%d/%s",
                postgresContainer.getHost(),
                postgresContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresContainer.getDatabaseName()
        );
        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresContainer::getUsername);
        registry.add("spring.r2dbc.password", postgresContainer::getPassword);

        // JDBC configuration for Flyway migrations (Flyway doesn't support R2DBC)
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Ensure Flyway uses the JDBC datasource
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
    }
}

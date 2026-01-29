package com.inetum.prices;

import com.inetum.prices.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Basic smoke test to verify that the Spring Boot application context loads successfully.
 * <p>
 * This test ensures that:
 * <ul>
 *   <li>All beans can be created without errors</li>
 *   <li>Flyway migrations run successfully</li>
 *   <li>JPA entity manager factory is properly configured</li>
 *   <li>Database connectivity works via Testcontainers</li>
 * </ul>
 */
class PricesApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
		// If this test completes successfully, the entire application context
		// was initialized without errors, proving the application is properly configured
		Assertions.assertTrue(true);
	}

}

package com.inetum.prices.application.config;

import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import com.inetum.prices.domain.ports.outbound.PriceRepositoryPort;
import com.inetum.prices.domain.service.PriceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class for wiring domain services.
 * <p>
 * This configuration class implements the hexagonal architecture principle of
 * Dependency Inversion: the domain layer defines interfaces (ports), and this
 * configuration class wires concrete implementations (adapters) without the
 * domain layer knowing about them.
 * <p>
 * <b>Architecture Benefits:</b>
 * <ul>
 *   <li>Domain layer remains framework-agnostic (no @Service annotations)</li>
 *   <li>Clear separation between ports and adapters</li>
 *   <li>Easy to swap implementations (e.g., mock repository for testing)</li>
 *   <li>Explicit dependency injection configuration</li>
 * </ul>
 */
@Configuration
public class PriceConfiguration {

    /**
     * Creates the GetPriceUseCase bean (domain service).
     * <p>
     * Spring will inject the PriceRepositoryPort implementation automatically.
     * The PostgreSQLPriceRepositoryAdapter is already a @Component, so Spring
     * will discover it and inject it here.
     *
     * @param priceRepository the repository port implementation (injected by Spring)
     * @return the configured use case
     */
    @Bean
    public GetPriceUseCase getPriceUseCase(PriceRepositoryPort priceRepository) {
        return new PriceService(priceRepository);
    }
}

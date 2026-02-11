package com.inetum.prices.application.config;

import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import com.inetum.prices.domain.ports.outbound.ProductPriceTimelineRepositoryPort;
import com.inetum.prices.domain.service.PriceService;
import com.inetum.prices.domain.service.mapper.PriceDomainMapper;
import com.inetum.prices.infrastructure.persistence.adapter.PostgreSQLProductPriceTimelineAdapter;
import com.inetum.prices.infrastructure.persistence.mapper.ProductPriceTimelineEntityMapper;
import com.inetum.prices.infrastructure.persistence.repository.SpringDataProductPriceTimelineRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

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
 * <p>
 * <b>CQRS Migration:</b>
 * This configuration now wires the new JSONB-based ProductPriceTimeline repository
 * as the primary implementation, replacing the old row-per-price pattern.
 */
@Configuration
public class PriceConfiguration {

    /**
     * Creates the ProductPriceTimelineRepositoryPort adapter (CQRS implementation).
     * <p>
     * This is marked as @Primary to make it the default implementation.
     * The adapter uses JSONB storage for O(1) database lookups.
     * <p>
     * Using @Lazy to prevent eager classloading of repository during configuration
     * introspection phase, which can cause ClassNotFoundException when JPA
     * infrastructure isn't ready yet.
     *
     * @param repository Spring Data repository for database access
     * @param mapper MapStruct mapper for entity-domain conversion
     * @return the configured repository adapter
     */
    @Bean
    @Primary
    @Lazy
    public ProductPriceTimelineRepositoryPort productPriceTimelineRepositoryPort(
            SpringDataProductPriceTimelineRepository repository,
            ProductPriceTimelineEntityMapper mapper) {
        return new PostgreSQLProductPriceTimelineAdapter(repository, mapper);
    }

    /**
     * Creates the GetPriceUseCase bean (domain service).
     * <p>
     * This service now uses ProductPriceTimeline aggregate for in-memory
     * price resolution instead of SQL-based filtering.
     *
     * @param timelineRepository the CQRS repository implementation
     * @param priceDomainMapper the mapper for converting PriceRule to Price
     * @return the configured use case
     */
    @Bean
    public GetPriceUseCase getPriceUseCase(
            ProductPriceTimelineRepositoryPort timelineRepository,
            PriceDomainMapper priceDomainMapper) {
        return new PriceService(timelineRepository, priceDomainMapper);
    }

}

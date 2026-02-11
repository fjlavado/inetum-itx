package com.inetum.prices.application.config;

import com.inetum.prices.infrastructure.persistence.converter.PriceRulesR2dbcConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.List;

/**
 * R2DBC configuration for reactive database access.
 * <p>
 * This configuration class sets up Spring Data R2DBC with PostgreSQL,
 * registering custom converters for JSONB column handling and enabling
 * reactive repository support.
 * <p>
 * <b>Key Configurations:</b>
 * <ul>
 *   <li>Enables R2DBC repositories for reactive data access</li>
 *   <li>Registers custom converters for List<PriceRule> ↔ PostgreSQL JSONB</li>
 *   <li>Leverages R2DBC's non-blocking connection pooling</li>
 * </ul>
 * <p>
 * <b>Performance Characteristics:</b>
 * <ul>
 *   <li>Non-blocking I/O for all database operations</li>
 *   <li>Connection pooling managed by r2dbc-pool</li>
 *   <li>Backpressure support via Project Reactor</li>
 * </ul>
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.inetum.prices.infrastructure.persistence.repository")
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfiguration(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    /**
     * Registers custom R2DBC converters for domain-specific type conversions.
     * <p>
     * This method registers converters for handling PostgreSQL JSONB columns,
     * specifically for the List<PriceRule> type used in ProductPriceTimeline entities.
     * <p>
     * <b>Registered Converters:</b>
     * <ul>
     *   <li>PriceRulesToJsonConverter: List<PriceRule> → PostgreSQL JSONB</li>
     *   <li>JsonToPriceRulesConverter: PostgreSQL JSONB → List<PriceRule></li>
     * </ul>
     *
     * @return R2dbcCustomConversions with registered custom converters
     */
    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(
                getStoreConversions(),
                List.of(
                        new PriceRulesR2dbcConverter.PriceRulesToJsonConverter(),
                        new PriceRulesR2dbcConverter.JsonToPriceRulesConverter()
                )
        );
    }
}

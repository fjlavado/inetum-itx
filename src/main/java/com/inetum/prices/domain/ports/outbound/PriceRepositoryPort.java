package com.inetum.prices.domain.ports.outbound;

import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound Port (Repository Interface) for price persistence operations.
 * <p>
 * This interface defines the contract that the domain expects from the infrastructure
 * layer for retrieving price data. It is the SECONDARY PORT in hexagonal architecture.
 * <p>
 * <b>Hexagonal Architecture:</b> This is a SECONDARY PORT - it represents
 * what the domain needs from the outside world (driven adapters like databases).
 * The infrastructure layer will provide concrete implementations.
 * <p>
 * <b>Design Rationale:</b>
 * <ul>
 *   <li>Domain defines the interface, infrastructure implements it (Dependency Inversion)</li>
 *   <li>No JPA, SQL, or database concepts - pure domain language</li>
 *   <li>Returns domain objects (Price), not entities or DTOs</li>
 *   <li>Focused query methods for specific use cases</li>
 * </ul>
 * <p>
 * <b>Implementation Notes:</b> Implementations should:
 * <ul>
 *   <li>Query by brand_id, product_id, and date range overlap</li>
 *   <li>Use appropriate database indexes for performance</li>
 *   <li>Map infrastructure entities to domain models</li>
 *   <li>Handle database exceptions and translate to domain exceptions if needed</li>
 * </ul>
 */
public interface PriceRepositoryPort {

    /**
     * Finds all prices that are applicable for a given product, brand, and date/time.
     * <p>
     * A price is applicable if:
     * <ul>
     *   <li>It matches the specified brand and product</li>
     *   <li>The application date falls within its [startDate, endDate] range</li>
     * </ul>
     * <p>
     * The returned list may contain multiple prices with different priorities.
     * The caller is responsible for selecting the appropriate one based on priority.
     *
     * @param applicationDate the date/time to check applicability for
     * @param productId       the product identifier
     * @param brandId         the brand identifier
     * @return list of applicable prices (may be empty, never null)
     * @throws IllegalArgumentException if any parameter is null
     */
    List<Price> findApplicablePrices(LocalDateTime applicationDate, ProductId productId, BrandId brandId);
}

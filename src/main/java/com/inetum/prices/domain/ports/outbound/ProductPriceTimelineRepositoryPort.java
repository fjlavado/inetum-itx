package com.inetum.prices.domain.ports.outbound;

import com.inetum.prices.domain.model.ProductPriceTimeline;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.util.Optional;

/**
 * Repository port for ProductPriceTimeline aggregate.
 * <p>
 * This port defines the contract for retrieving pricing timelines from
 * the persistence layer. The implementation uses JSONB storage in PostgreSQL
 * for optimal read performance.
 * <p>
 * <b>CQRS Pattern:</b>
 * This is a read-optimized port. The query pattern is simple: fetch the
 * entire timeline by product+brand composite key. All filtering happens
 * in-memory at the domain layer.
 * <p>
 * <b>Implementation Notes:</b>
 * <ul>
 * <li>Database query: O(1) primary key lookup</li>
 * <li>JSONB deserialization happens transparently via JPA converter</li>
 * <li>Returns empty Optional if no timeline exists for product+brand</li>
 * </ul>
 */
public interface ProductPriceTimelineRepositoryPort {

    /**
     * Finds the pricing timeline for a specific product and brand.
     * <p>
     * This performs a single database query using the composite primary key
     * (product_id, brand_id) and deserializes the JSONB rules into a
     * ProductPriceTimeline aggregate.
     *
     * @param productId the product identifier
     * @param brandId   the brand identifier
     * @return Optional containing the timeline if found, empty otherwise
     */
    Optional<ProductPriceTimeline> findByProductAndBrand(ProductId productId, BrandId brandId);

    /**
     * Saves or updates a pricing timeline.
     * <p>
     * This method is responsible for persisting the aggregate and ensuring
     * any associated caches are invalidated or updated.
     *
     * @param timeline the timeline to save
     */
    void save(ProductPriceTimeline timeline);
}

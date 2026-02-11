package com.inetum.prices.domain.ports.outbound;

import com.inetum.prices.domain.model.ProductPriceTimeline;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import reactor.core.publisher.Mono;

/**
 * Repository port for ProductPriceTimeline aggregate.
 * <p>
 * This port defines the contract for retrieving pricing timelines from
 * the persistence layer. The implementation uses JSONB storage in PostgreSQL
 * with R2DBC for reactive, non-blocking data access.
 * <p>
 * <b>CQRS Pattern:</b>
 * This is a read-optimized port. The query pattern is simple: fetch the
 * entire timeline by product+brand composite key. All filtering happens
 * in-memory at the domain layer.
 * <p>
 * <b>Implementation Notes:</b>
 * <ul>
 *   <li>Database query: O(1) primary key lookup</li>
 *   <li>JSONB deserialization happens transparently via R2DBC converter</li>
 *   <li>Returns empty Mono if no timeline exists for product+brand</li>
 *   <li>Non-blocking reactive execution with backpressure support</li>
 * </ul>
 */
public interface ProductPriceTimelineRepositoryPort {

    /**
     * Finds the pricing timeline for a specific product and brand reactively.
     * <p>
     * This performs a single non-blocking database query using the composite
     * primary key (product_id, brand_id) and deserializes the JSONB rules
     * into a ProductPriceTimeline aggregate.
     *
     * @param productId the product identifier
     * @param brandId   the brand identifier
     * @return Mono emitting the timeline if found, empty Mono otherwise
     * @throws IllegalArgumentException if productId or brandId is null
     */
    Mono<ProductPriceTimeline> findByProductAndBrand(ProductId productId, BrandId brandId);
}

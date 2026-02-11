package com.inetum.prices.infrastructure.persistence.repository;

import com.inetum.prices.infrastructure.persistence.entity.ProductPriceTimelineEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for ProductPriceTimelineEntity.
 * <p>
 * This repository leverages Spring Data R2DBC for reactive, non-blocking
 * database access with automatic query generation for basic CRUD operations.
 * <p>
 * <b>Query Performance:</b>
 * The findByProductIdAndBrandId method performs an O(1) primary key lookup
 * using the composite index on (product_id, brand_id). Execution is fully
 * non-blocking with backpressure support via Project Reactor.
 */
@Repository
public interface SpringDataProductPriceTimelineRepository
        extends R2dbcRepository<ProductPriceTimelineEntity, Long> {

    /**
     * Finds a ProductPriceTimeline by product ID and brand ID reactively.
     * <p>
     * This query uses the composite primary key for optimal performance.
     * Custom @Query annotation required because R2DBC method name derivation
     * doesn't automatically handle composite keys as well as JPA.
     *
     * @param productId the product identifier
     * @param brandId the brand identifier
     * @return Mono emitting the entity if found, empty Mono otherwise
     */
    @Query("SELECT * FROM product_price_timelines WHERE product_id = :productId AND brand_id = :brandId")
    Mono<ProductPriceTimelineEntity> findByProductIdAndBrandId(
            @Param("productId") Long productId,
            @Param("brandId") Long brandId
    );
}

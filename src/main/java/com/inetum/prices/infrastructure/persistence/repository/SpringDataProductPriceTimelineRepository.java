package com.inetum.prices.infrastructure.persistence.repository;

import com.inetum.prices.infrastructure.persistence.entity.ProductPriceTimelineEntity;
import com.inetum.prices.infrastructure.persistence.entity.ProductPriceTimelineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for ProductPriceTimelineEntity.
 * <p>
 * This repository leverages Spring Data's automatic query generation
 * for basic CRUD operations and custom finder methods.
 * <p>
 * <b>Query Performance:</b>
 * The findByProductIdAndBrandId method performs an O(1) primary key lookup
 * using the composite index on (product_id, brand_id).
 */
@Repository
public interface SpringDataProductPriceTimelineRepository 
        extends JpaRepository<ProductPriceTimelineEntity, ProductPriceTimelineId> {

    /**
     * Finds a ProductPriceTimeline by product ID and brand ID.
     * <p>
     * This query uses the composite primary key for optimal performance.
     * 
     * @param productId the product identifier
     * @param brandId the brand identifier
     * @return Optional containing the entity if found, empty otherwise
     */
    Optional<ProductPriceTimelineEntity> findByProductIdAndBrandId(Long productId, Long brandId);
}

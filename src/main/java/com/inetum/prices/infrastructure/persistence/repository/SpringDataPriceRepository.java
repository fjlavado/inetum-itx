package com.inetum.prices.infrastructure.persistence.repository;

import com.inetum.prices.infrastructure.persistence.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for PriceEntity.
 * <p>
 * This interface leverages Spring Data's query derivation and custom JPQL queries
 * to efficiently retrieve price data from PostgreSQL.
 * <p>
 * <b>Performance Optimization:</b>
 * <ul>
 *   <li>Uses indexed columns (brand_id, product_id, start_date, end_date)</li>
 *   <li>Custom JPQL query for optimal performance</li>
 *   <li>Returns only matching records (no lazy loading issues)</li>
 * </ul>
 */
@Repository
public interface SpringDataPriceRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Finds all prices applicable for a given product, brand, and date/time.
     * <p>
     * A price is applicable if:
     * <ul>
     *   <li>brand_id matches</li>
     *   <li>product_id matches</li>
     *   <li>applicationDate is between start_date and end_date (inclusive)</li>
     * </ul>
     * <p>
     * <b>Query Optimization:</b> The composite index idx_prices_complete_lookup
     * (brand_id, product_id, start_date, end_date, priority DESC) will be used
     * for efficient query execution.
     *
     * @param brandId         the brand identifier
     * @param productId       the product identifier
     * @param applicationDate the date/time to check applicability for
     * @return list of applicable price entities (may be empty, never null)
     */
    @Query("SELECT p FROM PriceEntity p WHERE p.brandId = :brandId " +
            "AND p.productId = :productId " +
            "AND p.startDate <= :applicationDate " +
            "AND p.endDate >= :applicationDate " +
            "ORDER BY p.priority DESC")
    List<PriceEntity> findApplicablePrices(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("applicationDate") LocalDateTime applicationDate
    );
}

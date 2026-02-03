package com.inetum.prices.infrastructure.persistence.entity;

import com.inetum.prices.domain.model.PriceRule;
import com.inetum.prices.infrastructure.persistence.converter.PriceRulesJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for ProductPriceTimeline stored with JSONB column.
 * <p>
 * This entity represents the CQRS read model, storing all pricing rules
 * for a product+brand combination as a single JSONB document in PostgreSQL.
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 * <li>Composite primary key: (product_id, brand_id)</li>
 * <li>JSONB column for price_rules enables flexible storage and fast
 * queries</li>
 * <li>Optimistic locking via @Version for concurrent update protection</li>
 * <li>Timestamps for audit trail</li>
 * </ul>
 * <p>
 * <b>Performance Characteristics:</b>
 * <ul>
 * <li>Primary key lookup: O(1) via B-tree index</li>
 * <li>JSONB deserialization: O(n) where n = number of rules (typically <
 * 10)</li>
 * <li>GIN index on JSONB column enables advanced JSON queries if needed</li>
 * </ul>
 * <p>
 * <b>Table: product_price_timelines</b>
 * 
 * <pre>
 * CREATE TABLE product_price_timelines (
 *     product_id BIGINT NOT NULL,
 *     brand_id BIGINT NOT NULL,
 *     price_rules JSONB NOT NULL,
 *     version BIGINT NOT NULL DEFAULT 0,
 *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     PRIMARY KEY (product_id, brand_id)
 * );
 * </pre>
 */
@Entity
@Table(name = "product_price_timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductPriceTimelineId.class)
public class ProductPriceTimelineEntity {

    /**
     * Product identifier (part of composite primary key).
     */
    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Brand identifier (part of composite primary key).
     */
    @Id
    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    /**
     * List of pricing rules stored as JSONB.
     * <p>
     * The JSON structure allows PostgreSQL to efficiently store and query
     * nested data while maintaining type safety in the application layer.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = PriceRulesJsonConverter.class)
    @Column(name = "price_rules", columnDefinition = "jsonb", nullable = false)
    private List<PriceRule> priceRules;

    /**
     * Version for optimistic locking.
     * <p>
     * Prevents lost updates when multiple processes attempt to modify
     * the same product's pricing rules concurrently.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Timestamp when this record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Pre-persist callback to set creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Pre-update callback to update modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

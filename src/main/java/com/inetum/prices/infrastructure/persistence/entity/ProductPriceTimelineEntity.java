package com.inetum.prices.infrastructure.persistence.entity;

import com.inetum.prices.domain.model.PriceRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * R2DBC Entity for ProductPriceTimeline stored with JSONB column.
 * <p>
 * This entity represents the CQRS read model, storing all pricing rules
 * for a product+brand combination as a single JSONB document in PostgreSQL.
 * Uses R2DBC for reactive, non-blocking database access.
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 *   <li>Composite primary key: (product_id, brand_id)</li>
 *   <li>JSONB column for price_rules enables flexible storage and fast queries</li>
 *   <li>R2DBC converters handle JSONB serialization/deserialization</li>
 *   <li>Timestamps for audit trail</li>
 * </ul>
 * <p>
 * <b>Performance Characteristics:</b>
 * <ul>
 *   <li>Primary key lookup: O(1) via B-tree index</li>
 *   <li>JSONB deserialization: O(n) where n = number of rules (typically < 10)</li>
 *   <li>Non-blocking reactive execution with backpressure support</li>
 *   <li>GIN index on JSONB column enables advanced JSON queries if needed</li>
 * </ul>
 * <p>
 * <b>Table: product_price_timelines</b>
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
@Table("product_price_timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceTimelineEntity {

    /**
     * Product identifier (part of composite primary key).
     * Note: In R2DBC, we mark the first field of a composite key as @Id.
     * The actual composite key constraint is enforced by the database schema.
     */
    @Id
    @Column("product_id")
    private Long productId;

    /**
     * Brand identifier (part of composite primary key).
     */
    @Column("brand_id")
    private Long brandId;

    /**
     * List of pricing rules stored as JSONB.
     * <p>
     * The JSON structure allows PostgreSQL to efficiently store and query
     * nested data while maintaining type safety in the application layer.
     * <p>
     * Note: JSONB conversion is handled by custom R2DBC converter registered
     * in configuration.
     */
    @Column("price_rules")
    private List<PriceRule> priceRules;

    /**
     * Version for tracking updates.
     * <p>
     * Note: R2DBC doesn't have built-in optimistic locking like JPA,
     * but we keep this field for audit purposes.
     */
    @Column("version")
    private Long version;

    /**
     * Timestamp when this record was created.
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last updated.
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

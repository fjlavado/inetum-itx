package com.inetum.prices.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for ProductPriceTimelineEntity.
 * <p>
 * JPA requires a separate class for composite keys when using @IdClass annotation.
 * This class represents the combination of product_id and brand_id that uniquely
 * identifies a pricing timeline.
 * <p>
 * <b>Requirements for @IdClass:</b>
 * <ul>
 *   <li>Must be public</li>
 *   <li>Must implement Serializable</li>
 *   <li>Must have a no-arg constructor</li>
 *   <li>Must override equals() and hashCode()</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceTimelineId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Product identifier (part of composite key).
     */
    private Long productId;

    /**
     * Brand identifier (part of composite key).
     */
    private Long brandId;
}

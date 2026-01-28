package com.inetum.prices.domain.model;

import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;

/**
 * Price Aggregate Root representing a pricing rule in the e-commerce domain.
 * <p>
 * This is the central entity in the pricing bounded context, encapsulating all
 * business rules related to price applicability and priority resolution.
 * <p>
 * <b>Aggregate Root Responsibilities:</b>
 * <ul>
 *   <li>Maintains pricing invariants (valid date ranges, non-negative amounts)</li>
 *   <li>Encapsulates priority-based conflict resolution logic</li>
 *   <li>Determines temporal applicability of prices</li>
 *   <li>Ensures consistency across all price attributes</li>
 * </ul>
 * <p>
 * <b>Business Rules:</b>
 * <ul>
 *   <li>A price is applicable if the query date falls within [startDate, endDate]</li>
 *   <li>When multiple prices overlap, the one with highest priority wins</li>
 *   <li>Start date must be before end date</li>
 *   <li>All value objects must be valid (enforced by their own constructors)</li>
 * </ul>
 * <p>
 * This record is immutable, thread-safe, and contains no infrastructure dependencies.
 *
 * @param brandId     the brand this price applies to
 * @param productId   the product this price applies to
 * @param priceListId the price list identifier (categorizes the pricing strategy)
 * @param startDate   the date and time when this price becomes effective
 * @param endDate     the date and time when this price expires
 * @param priority    conflict resolution priority (higher wins)
 * @param amount      the price amount
 */
public record Price(
        BrandId brandId,
        ProductId productId,
        PriceListId priceListId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Priority priority,
        Money amount
) {

    /**
     * Compact constructor with invariant validation.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    public Price {
        if (brandId == null) {
            throw new IllegalArgumentException("BrandId cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (priceListId == null) {
            throw new IllegalArgumentException("PriceListId cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date. Got start: " + startDate + ", end: " + endDate
            );
        }
    }

    /**
     * Determines if this price is applicable at a given point in time.
     * <p>
     * A price is applicable if the application date falls within the inclusive
     * range [startDate, endDate].
     *
     * @param applicationDate the date to check applicability for
     * @return true if this price is applicable at the given date
     * @throws IllegalArgumentException if applicationDate is null
     */
    public boolean isApplicableAt(LocalDateTime applicationDate) {
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null");
        }

        return (applicationDate.isEqual(startDate) || applicationDate.isAfter(startDate)) &&
                (applicationDate.isEqual(endDate) || applicationDate.isBefore(endDate));
    }

    /**
     * Determines if this price has higher priority than another price.
     * <p>
     * This method is used for conflict resolution when multiple prices
     * are applicable for the same product/brand/time.
     *
     * @param other the other Price to compare to
     * @return true if this price has strictly higher priority
     * @throws IllegalArgumentException if other is null
     */
    public boolean hasHigherPriorityThan(Price other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare priority with null Price");
        }
        return this.priority.isHigherThan(other.priority);
    }

    /**
     * Determines if this price has lower priority than another price.
     *
     * @param other the other Price to compare to
     * @return true if this price has strictly lower priority
     * @throws IllegalArgumentException if other is null
     */
    public boolean hasLowerPriorityThan(Price other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare priority with null Price");
        }
        return this.priority.isLowerThan(other.priority);
    }

    /**
     * Checks if this price matches a specific brand and product.
     *
     * @param brandId   the brand to match
     * @param productId the product to match
     * @return true if both brand and product match
     */
    public boolean matchesBrandAndProduct(BrandId brandId, ProductId productId) {
        return this.brandId.equals(brandId) && this.productId.equals(productId);
    }
}

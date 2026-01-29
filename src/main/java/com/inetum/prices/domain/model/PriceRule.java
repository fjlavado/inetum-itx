package com.inetum.prices.domain.model;

import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;

import java.time.LocalDateTime;

/**
 * Value Object representing a single pricing rule within a ProductPriceTimeline.
 * <p>
 * In the CQRS architecture, this represents one pricing rule that was formerly
 * stored as an entire row in the prices table. Now multiple PriceRules are
 * aggregated into a single ProductPriceTimeline and stored as JSONB.
 * <p>
 * <b>Design Characteristics:</b>
 * <ul>
 *   <li>Immutable Value Object (Java Record)</li>
 *   <li>No infrastructure dependencies</li>
 *   <li>Serializable to JSON for JSONB storage</li>
 *   <li>Contains validation logic in compact constructor</li>
 * </ul>
 * <p>
 * <b>Business Rules:</b>
 * <ul>
 *   <li>Start date must be before end date</li>
 *   <li>All fields are mandatory (no nulls)</li>
 *   <li>Priority determines conflict resolution (higher wins)</li>
 *   <li>Temporal applicability checked via isApplicableAt()</li>
 * </ul>
 *
 * @param priceListId the price list identifier
 * @param startDate   when this rule becomes effective
 * @param endDate     when this rule expires
 * @param priority    conflict resolution priority (higher wins)
 * @param amount      the price amount
 */
public record PriceRule(
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
    public PriceRule {
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
     * Determines if this pricing rule is applicable at a given point in time.
     * <p>
     * A rule is applicable if the application date falls within the inclusive
     * range [startDate, endDate].
     *
     * @param applicationDate the date to check applicability for
     * @return true if this rule is applicable at the given date
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
     * Determines if this rule has higher priority than another rule.
     * <p>
     * Used for conflict resolution when multiple rules are applicable.
     *
     * @param other the other PriceRule to compare to
     * @return true if this rule has strictly higher priority
     * @throws IllegalArgumentException if other is null
     */
    public boolean hasHigherPriorityThan(PriceRule other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare priority with null PriceRule");
        }
        return this.priority.isHigherThan(other.priority);
    }

    /**
     * Determines if this rule has lower priority than another rule.
     *
     * @param other the other PriceRule to compare to
     * @return true if this rule has strictly lower priority
     * @throws IllegalArgumentException if other is null
     */
    public boolean hasLowerPriorityThan(PriceRule other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare priority with null PriceRule");
        }
        return this.priority.isLowerThan(other.priority);
    }
}

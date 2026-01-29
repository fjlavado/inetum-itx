package com.inetum.prices.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing monetary amounts in the pricing domain.
 * <p>
 * Wraps a BigDecimal value with proper validation and rounding to ensure
 * financial calculations are accurate and consistent.
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 *   <li>Uses BigDecimal to avoid floating-point precision issues</li>
 *   <li>Enforces 2 decimal places (standard for EUR currency)</li>
 *   <li>Prevents negative amounts</li>
 *   <li>Immutable by design</li>
 * </ul>
 *
 * @param amount the monetary amount (must be non-negative, 2 decimal places)
 */
public record Money(BigDecimal amount) implements SingleValueObject<BigDecimal>, Comparable<Money> {

    /**
     * Compact constructor with validation and normalization.
     *
     * @throws IllegalArgumentException if amount is null or negative
     */
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Money amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative, got: " + amount);
        }
        // Normalize to 2 decimal places for consistency
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Creates Money from a BigDecimal value.
     *
     * @param amount the monetary amount
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /**
     * Creates Money from a double value (convenience method).
     * <p>
     * Note: Use with caution due to floating-point precision issues.
     * Prefer {@link #of(BigDecimal)} when possible.
     *
     * @param amount the monetary amount as double
     * @return a new Money instance
     */
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    /**
     * Returns the wrapped BigDecimal value.
     * <p>
     * Overrides the default value() method to return amount instead.
     *
     * @return the monetary amount
     */
    @Override
    public BigDecimal value() {
        return amount;
    }

    /**
     * Compares this money amount with another for ordering.
     *
     * @param other the Money to compare to
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    /**
     * Checks if this amount is greater than another.
     *
     * @param other the Money to compare to
     * @return true if this amount is greater
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this amount is less than another.
     *
     * @param other the Money to compare to
     * @return true if this amount is less
     */
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Returns a string representation suitable for display.
     *
     * @return formatted string (e.g., "35.50")
     */
    @Override
    public String toString() {
        return amount.toPlainString();
    }
}

package com.inetum.prices.domain.model.valueobject;

import com.inetum.prices.domain.exception.DomainValidationException;

/**
 * Value Object representing the priority of a price entry.
 * <p>
 * Priority determines which price should be applied when multiple price lists
 * overlap
 * for the same product, brand, and time period. Higher values indicate higher
 * priority.
 * <p>
 * <b>Business Rule:</b> When multiple prices are applicable, the one with the
 * highest
 * priority value wins.
 * <p>
 * This record is immutable and implements Comparable for natural ordering.
 *
 * @param value the priority value (must be non-negative, higher is better)
 */
public record Priority(Integer value) implements SingleValueObject<Integer>, Comparable<Priority> {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if value is null or negative
     */
    public Priority {
        if (value == null) {
            throw new DomainValidationException("Priority cannot be null");
        }
        if (value < 0) {
            throw new DomainValidationException("Priority must be non-negative");
        }
    }

    /**
     * Creates a Priority from a primitive integer.
     *
     * @param value the priority value
     * @return a new Priority instance
     */
    public static Priority of(Integer value) {
        return new Priority(value);
    }

    /**
     * Compares this priority with another for ordering.
     * Higher priority values are considered "greater than" lower values.
     *
     * @param other the Priority to compare to
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(Priority other) {
        return this.value.compareTo(other.value);
    }

    /**
     * Checks if this priority is higher than another.
     *
     * @param other the Priority to compare to
     * @return true if this priority has a higher value
     */
    public boolean isHigherThan(Priority other) {
        return this.value > other.value;
    }

    /**
     * Checks if this priority is lower than another.
     *
     * @param other the Priority to compare to
     * @return true if this priority has a lower value
     */
    public boolean isLowerThan(Priority other) {
        return this.value < other.value;
    }
}

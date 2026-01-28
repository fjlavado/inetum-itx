package com.inetum.prices.domain.model.valueobject;

/**
 * Value Object representing a Product identifier.
 * <p>
 * Wraps a Long value to provide type safety and prevent primitive obsession.
 * Ensures that product IDs cannot be confused with brand IDs or other numeric values.
 * <p>
 * This record is immutable and validates the product ID in its compact constructor.
 *
 * @param value the product identifier (must be positive)
 */
public record ProductId(Long value) implements SingleValueObject<Long> {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if value is null or non-positive
     */
    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("ProductId must be positive, got: " + value);
        }
    }

    /**
     * Creates a ProductId from a primitive long.
     *
     * @param value the product identifier
     * @return a new ProductId instance
     */
    public static ProductId of(Long value) {
        return new ProductId(value);
    }
}

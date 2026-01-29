package com.inetum.prices.domain.model.valueobject;

/**
 * Value Object representing a Brand identifier.
 * <p>
 * Wraps a Long value to provide type safety and prevent primitive obsession.
 * For example, brand 1 represents ZARA in the e-commerce domain.
 * <p>
 * This record is immutable and validates the brand ID in its compact constructor.
 *
 * @param value the brand identifier (must be positive)
 */
public record BrandId(Long value) implements SingleValueObject<Long> {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if value is null or non-positive
     */
    public BrandId {
        if (value == null) {
            throw new IllegalArgumentException("BrandId cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("BrandId must be positive, got: " + value);
        }
    }

    /**
     * Creates a BrandId from a primitive long.
     *
     * @param value the brand identifier
     * @return a new BrandId instance
     */
    public static BrandId of(Long value) {
        return new BrandId(value);
    }
}

package com.inetum.prices.domain.model.valueobject;

import com.inetum.prices.domain.exception.DomainValidationException;

/**
 * Value Object representing a Brand identifier.
 * <p>
 * Wraps a Long value to provide type safety and prevent primitive obsession.
 * For example, brand 1 represents ZARA in the e-commerce domain.
 * <p>
 * This record is immutable and validates the brand ID in its compact
 * constructor.
 * Value object for Brand ID.
 */
public record BrandId(Long value) implements SingleValueObject<Long> {
    public BrandId {
        if (value == null) {
            throw new DomainValidationException("BrandId cannot be null");
        }
        if (value <= 0) {
            throw new DomainValidationException("BrandId must be positive");
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

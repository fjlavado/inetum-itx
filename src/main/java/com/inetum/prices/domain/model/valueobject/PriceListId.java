package com.inetum.prices.domain.model.valueobject;

import com.inetum.prices.domain.exception.DomainValidationException;

/**
 * Value Object representing a Price List identifier.
 * <p>
 * Wraps an Integer value to provide type safety and prevent primitive
 * obsession.
 * Price lists are used to categorize different pricing strategies (base,
 * promotional, premium, etc.).
 * <p>
 * This record is immutable and validates the price list ID in its compact
 * constructor.
 *
 * @param value the price list identifier (must be positive)
 */
public record PriceListId(Integer value) implements SingleValueObject<Integer> {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if value is null or non-positive
     */
    public PriceListId {
        if (value == null) {
            throw new DomainValidationException("PriceListId cannot be null");
        }
        if (value <= 0) {
            throw new DomainValidationException("PriceListId must be positive");
        }
    }

    /**
     * Creates a PriceListId from a primitive integer.
     *
     * @param value the price list identifier
     * @return a new PriceListId instance
     */
    public static PriceListId of(Integer value) {
        return new PriceListId(value);
    }
}

package com.inetum.prices.domain.model.valueobject;

/**
 * Marker interface for Single Value Objects following DDD tactical patterns.
 * <p>
 * This interface enforces type safety and prevents primitive obsession by wrapping
 * primitive types (Long, Integer, BigDecimal) into domain-meaningful types.
 * <p>
 * <b>Design Rationale:</b>
 * <ul>
 *   <li>Prevents passing wrong primitive values (e.g., passing productId where brandId is expected)</li>
 *   <li>Centralizes validation logic in the value object itself</li>
 *   <li>Makes the domain model more expressive and self-documenting</li>
 *   <li>Leverages Java Records for immutability and compact syntax</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * public record BrandId(Long value) implements SingleValueObject<Long> {
 *     public BrandId {
 *         if (value == null || value <= 0) {
 *             throw new IllegalArgumentException("BrandId must be positive");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the wrapped value (e.g., Long, Integer, BigDecimal)
 * @author ITX Engineering Team
 * @since 1.0
 */
public interface SingleValueObject<T> {

    /**
     * Returns the wrapped primitive value.
     *
     * @return the underlying value
     */
    T value();
}

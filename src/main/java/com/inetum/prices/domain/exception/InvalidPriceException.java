package com.inetum.prices.domain.exception;

/**
 * Domain exception thrown when price data violates business invariants.
 * <p>
 * This exception represents validation failures at the domain level, such as:
 * <ul>
 *   <li>Invalid date ranges (start date after end date)</li>
 *   <li>Negative prices</li>
 *   <li>Invalid priority values</li>
 *   <li>Null required fields</li>
 * </ul>
 * <p>
 * This is typically thrown during Price aggregate construction or value object
 * creation when invariants are violated.
 */
public class InvalidPriceException extends RuntimeException {

    /**
     * Constructs a new InvalidPriceException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidPriceException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPriceException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InvalidPriceException(String message, Throwable cause) {
        super(message, cause);
    }
}

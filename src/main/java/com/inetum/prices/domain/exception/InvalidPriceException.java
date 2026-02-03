package com.inetum.prices.domain.exception;

/**
 * Exception thrown when price data violates domain business rules.
 */
public class InvalidPriceException extends DomainErrorException {

    /**
     * Constructs a new InvalidPriceException with a specific message.
     *
     * @param message the detail message
     */
    public InvalidPriceException(String message) {
        super(message);
    }
}

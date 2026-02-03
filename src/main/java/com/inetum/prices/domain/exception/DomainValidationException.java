package com.inetum.prices.domain.exception;

/**
 * Exception thrown when domain validation rules are violated.
 * This replaces standard IllegalArgumentException when the source of the error
 * is the domain logic.
 */
public class DomainValidationException extends DomainErrorException {
    public DomainValidationException(String message) {
        super(message);
    }
}

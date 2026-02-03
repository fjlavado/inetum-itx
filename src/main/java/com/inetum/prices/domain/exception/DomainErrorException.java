package com.inetum.prices.domain.exception;

/**
 * Base class for all domain-related exceptions.
 * This ensures that business logic errors are clearly distinguished from
 * technical or infrastructure errors.
 */
public abstract class DomainErrorException extends RuntimeException {
    protected DomainErrorException(String message) {
        super(message);
    }
}

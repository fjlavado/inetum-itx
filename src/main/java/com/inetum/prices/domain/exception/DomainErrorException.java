package com.inetum.prices.domain.exception;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class DomainErrorException extends RuntimeException  {


    DomainErrorException(String message) {
        super(message);
    }

    public DomainErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public static Optional<DomainErrorException> requireNonNull(Object value, String message) {
        return require(() -> !Objects.isNull(value), message);
    }

    public static Optional<DomainErrorException> require(BooleanSupplier predicate, String message) {
        if (!predicate.getAsBoolean()) {
            return Optional.of(new DomainErrorException(message));
        }
        return Optional.empty();
    }
}


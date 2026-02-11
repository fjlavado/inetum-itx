package com.inetum.prices.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DomainErrorException}.
 * <p>
 * Tests verify exception creation, validation helpers, and error handling utilities.
 */
@DisplayName("DomainErrorException Tests")
class DomainErrorExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Domain validation failed";

        // When
        DomainErrorException exception = new DomainErrorException(message);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Domain validation failed";
        Throwable cause = new IllegalStateException("Invalid state");

        // When
        DomainErrorException exception = new DomainErrorException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid state");
    }

    @Test
    @DisplayName("Should return exception when requireNonNull receives null value")
    void shouldReturnExceptionWhenRequireNonNullReceivesNull() {
        // Given
        Object nullValue = null;
        String message = "Value cannot be null";

        // When
        Optional<DomainErrorException> result = DomainErrorException.requireNonNull(nullValue, message);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should return empty when requireNonNull receives non-null value")
    void shouldReturnEmptyWhenRequireNonNullReceivesNonNull() {
        // Given
        Object nonNullValue = "some value";
        String message = "Value cannot be null";

        // When
        Optional<DomainErrorException> result = DomainErrorException.requireNonNull(nonNullValue, message);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return exception when require predicate evaluates to false")
    void shouldReturnExceptionWhenRequirePredicateIsFalse() {
        // Given
        String message = "Validation failed";

        // When
        Optional<DomainErrorException> result = DomainErrorException.require(() -> false, message);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should return empty when require predicate evaluates to true")
    void shouldReturnEmptyWhenRequirePredicateIsTrue() {
        // Given
        String message = "Validation failed";

        // When
        Optional<DomainErrorException> result = DomainErrorException.require(() -> true, message);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle complex predicate logic in require method")
    void shouldHandleComplexPredicateLogicInRequire() {
        // Given
        int value = 5;
        String message = "Value must be greater than 10";

        // When - Check if value > 10 (requirement), should fail for value=5
        Optional<DomainErrorException> result = DomainErrorException.require(() -> value > 10, message);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should work with requireNonNull in validation chain")
    void shouldWorkWithRequireNonNullInValidationChain() {
        // Given
        String message = "Field is required";

        // When - Simulate validation chain
        Optional<DomainErrorException> error1 = DomainErrorException.requireNonNull(null, message);
        Optional<DomainErrorException> error2 = DomainErrorException.requireNonNull("valid", message);

        // Then
        assertThat(error1).isPresent();
        assertThat(error2).isEmpty();
    }

    @Test
    @DisplayName("Should maintain exception message integrity")
    void shouldMaintainExceptionMessageIntegrity() {
        // Given
        String originalMessage = "Original error message with special chars: !@#$%";

        // When
        DomainErrorException exception = new DomainErrorException(originalMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(originalMessage);
    }

    @Test
    @DisplayName("Should handle null message in constructor")
    void shouldHandleNullMessageInConstructor() {
        // When
        DomainErrorException exception = new DomainErrorException(null);

        // Then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle null cause in constructor")
    void shouldHandleNullCauseInConstructor() {
        // Given
        String message = "Error message";

        // When
        DomainErrorException exception = new DomainErrorException(message, null);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }
}

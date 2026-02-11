package com.inetum.prices.application.rest.exception;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * <p>
 * Tests verify that all exception handlers return RFC 9457-compliant ProblemDetail responses
 * with appropriate HTTP status codes, titles, and detail messages.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle PriceNotFoundException with 404 status")
    void shouldHandlePriceNotFoundException() {
        // Given
        PriceNotFoundException exception = new PriceNotFoundException(
                "Price not found for product 35455 and brand 1 at 2020-06-14T10:00:00"
        );

        // When
        ResponseEntity<ProblemDetail> response = handler.handlePriceNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getTitle()).isEqualTo("Price Not Found");
        assertThat(response.getBody().getDetail()).contains("Price not found");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }


    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Date cannot be null");

        // When
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgument(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
        assertThat(response.getBody().getDetail()).contains("Invalid request parameters");
        assertThat(response.getBody().getDetail()).contains("Date cannot be null");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with 400 status")
    void shouldHandleMethodArgumentTypeMismatchException() {
        // Given
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "invalid",
                Long.class,
                "productId",
                parameter,
                null
        );

        // When
        ResponseEntity<ProblemDetail> response = handler.handleTypeMismatch(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
        assertThat(response.getBody().getDetail()).contains("Invalid value for parameter 'productId'");
        assertThat(response.getBody().getDetail()).contains("Long");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with null required type")
    void shouldHandleMethodArgumentTypeMismatchExceptionWithNullType() {
        // Given
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "invalid",
                null, // null required type
                "productId",
                parameter,
                null
        );

        // When
        ResponseEntity<ProblemDetail> response = handler.handleTypeMismatch(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("unknown");
    }

    @Test
    @DisplayName("Should handle ServerWebInputException with 400 status")
    void shouldHandleServerWebInputException() {
        // Given
        ServerWebInputException exception = mock(ServerWebInputException.class);
        when(exception.getReason()).thenReturn("Required parameter 'applicationDate' is missing");
        when(exception.getMessage()).thenReturn("Fallback message");

        // When
        ResponseEntity<ProblemDetail> response = handler.handleServerWebInputException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
        assertThat(response.getBody().getDetail()).isEqualTo("Required parameter 'applicationDate' is missing");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle ServerWebInputException with null reason")
    void shouldHandleServerWebInputExceptionWithNullReason() {
        // Given
        ServerWebInputException exception = mock(ServerWebInputException.class);
        when(exception.getReason()).thenReturn(null);
        when(exception.getMessage()).thenReturn("Exception message");

        // When
        ResponseEntity<ProblemDetail> response = handler.handleServerWebInputException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Exception message");
    }

    @Test
    @DisplayName("Should handle WebExchangeBindException with field errors")
    void shouldHandleWebExchangeBindException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("priceRequest", "productId", "must not be null");
        FieldError fieldError2 = new FieldError("priceRequest", "brandId", "must be greater than 0");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodParameter parameter = mock(MethodParameter.class);
        WebExchangeBindException exception = new WebExchangeBindException(parameter, bindingResult);

        // When
        ResponseEntity<ProblemDetail> response = handler.handleWebExchangeBindException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getDetail()).contains("productId: must not be null");
        assertThat(response.getBody().getDetail()).contains("brandId: must be greater than 0");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle WebExchangeBindException with empty field errors")
    void shouldHandleWebExchangeBindExceptionWithEmptyErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodParameter parameter = mock(MethodParameter.class);
        // Mock the executable to avoid NPE when getMessage() is called
        when(parameter.getExecutable()).thenReturn(
                GlobalExceptionHandlerTest.class.getDeclaredMethods()[0]
        );

        WebExchangeBindException exception = new WebExchangeBindException(parameter, bindingResult);

        // When
        ResponseEntity<ProblemDetail> response = handler.handleWebExchangeBindException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).startsWith("Validation failed:");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with violations")
    void shouldHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("getPrice.productId");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must be greater than or equal to 1");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("getPrice.brandId");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("must not be null");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        // When
        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getTitle()).isEqualTo("Constraint Violation");
        assertThat(response.getBody().getDetail()).contains("getPrice.productId");
        assertThat(response.getBody().getDetail()).contains("getPrice.brandId");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with empty violations")
    void shouldHandleConstraintViolationExceptionWithEmptyViolations() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        // When
        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).startsWith("Validation failed:");
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void shouldHandleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected database error");

        // When
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getDetail()).contains("An unexpected error occurred");
        assertThat(response.getBody().getDetail()).contains("Unexpected database error");
        assertThat(response.getBody().getType()).isEqualTo(URI.create("about:blank"));
    }

    @Test
    @DisplayName("Should handle Exception with null message")
    void shouldHandleGenericExceptionWithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);

        // When
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).contains("An unexpected error occurred");
    }
}

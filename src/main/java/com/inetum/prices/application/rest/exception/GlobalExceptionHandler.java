package com.inetum.prices.application.rest.exception;

import com.inetum.prices.domain.exception.DomainValidationException;
import com.inetum.prices.domain.exception.InvalidPriceException;
import com.inetum.prices.domain.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Optional;

/**
 * Global exception handler for the REST API using ProblemDetail (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Price Not Found");
        problemDetail.setType(URI.create("https://api.prices.com/errors/not-found"));
        return problemDetail;
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ProblemDetail handleInvalidPrice(InvalidPriceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid Price Request");
        problemDetail.setType(URI.create("https://api.prices.com/errors/bad-request"));
        return problemDetail;
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Domain Validation Error");
        problemDetail.setType(URI.create("https://api.prices.com/errors/validation-error"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Illegal Argument");
        problemDetail.setType(URI.create("https://api.prices.com/errors/illegal-argument"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Invalid value for parameter '%s': expected type %s",
                ex.getName(),
                Optional.of(ex).map(MethodArgumentTypeMismatchException::getRequiredType)
                        .map(Class::getSimpleName).orElse("unknown"));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setType(URI.create("https://api.prices.com/errors/type-mismatch"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.prices.com/errors/internal-server-error"));
        return problemDetail;
    }
}

package com.inetum.prices.application.rest.exception;

import com.inetum.prices.application.rest.dto.ErrorResponse;
import com.inetum.prices.domain.exception.InvalidPriceException;
import com.inetum.prices.domain.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Global exception handler for the REST API.
 * <p>
 * This class uses Spring's @RestControllerAdvice to catch exceptions thrown by
 * controllers and convert them into appropriate HTTP error responses.
 * <p>
 * <b>Exception Mapping Strategy:</b>
 * <ul>
 *   <li>PriceNotFoundException → 404 NOT FOUND</li>
 *   <li>InvalidPriceException → 400 BAD REQUEST</li>
 *   <li>IllegalArgumentException → 400 BAD REQUEST</li>
 *   <li>MethodArgumentTypeMismatchException → 400 BAD REQUEST</li>
 *   <li>Generic Exception → 500 INTERNAL SERVER ERROR</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles PriceNotFoundException - when no applicable price is found.
     *
     * @param ex the exception
     * @return 404 NOT FOUND response
     */
    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFound(PriceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles InvalidPriceException - when price data violates business rules.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response
     */
    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPrice(InvalidPriceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles IllegalArgumentException - when input parameters are invalid.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request parameters: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles MethodArgumentTypeMismatchException - when query parameter types are wrong.
     * <p>
     * Example: passing a non-numeric value for productId.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Invalid value for parameter '%s': expected type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the exception
     * @return 500 INTERNAL SERVER ERROR response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

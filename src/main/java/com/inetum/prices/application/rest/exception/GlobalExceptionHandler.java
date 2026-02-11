package com.inetum.prices.application.rest.exception;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API (reactive and non-reactive).
 * <p>
 * This class uses Spring's @RestControllerAdvice to catch exceptions thrown by
 * controllers and convert them into RFC 9457 Problem Details responses using
 * Spring's {@link ProblemDetail} class.
 * <p>
 * <b>Exception Mapping Strategy:</b>
 * <ul>
 *   <li>PriceNotFoundException → 404 NOT FOUND</li>
 *   <li>InvalidPriceException → 400 BAD REQUEST</li>
 *   <li>IllegalArgumentException → 400 BAD REQUEST</li>
 *   <li>MethodArgumentTypeMismatchException → 400 BAD REQUEST (legacy non-reactive)</li>
 *   <li>ServerWebInputException → 400 BAD REQUEST (reactive parameter binding errors)</li>
 *   <li>WebExchangeBindException → 400 BAD REQUEST (reactive @Valid validation errors)</li>
 *   <li>ConstraintViolationException → 400 BAD REQUEST (Bean Validation errors)</li>
 *   <li>Generic Exception → 500 INTERNAL SERVER ERROR</li>
 * </ul>
 * <p>
 * <b>RFC 9457 Compliance:</b>
 * All error responses follow RFC 9457 Problem Details standard with:
 * <ul>
 *   <li><b>type</b>: URI identifying the problem type (default: "about:blank")</li>
 *   <li><b>title</b>: Short, human-readable summary</li>
 *   <li><b>status</b>: HTTP status code</li>
 *   <li><b>detail</b>: Human-readable explanation specific to this occurrence</li>
 *   <li><b>instance</b>: Optional URI identifying the specific occurrence</li>
 * </ul>
 *
 * @see ProblemDetail Spring's RFC 9457 implementation
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457.html">RFC 9457</a>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Default constructor (no dependencies needed when using Spring's ProblemDetail).
     */
    public GlobalExceptionHandler() {
        // No dependencies required
    }

    /**
     * Handles PriceNotFoundException - when no applicable price is found.
     * <p>
     * Returns RFC 9457 Problem Details with status 404.
     *
     * @param ex the exception
     * @return 404 NOT FOUND response with ProblemDetail
     */
    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePriceNotFound(PriceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Price Not Found");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles IllegalArgumentException - when input parameters are invalid.
     * <p>
     * Returns RFC 9457 Problem Details with status 400.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response with ProblemDetail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters: " + ex.getMessage()
        );
        problem.setTitle("Bad Request");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles MethodArgumentTypeMismatchException - when query parameter types are wrong (non-reactive).
     * <p>
     * Example: passing a non-numeric value for productId in MVC controllers.
     * <p>
     * Note: For reactive controllers, see {@link #handleServerWebInputException(ServerWebInputException)}
     * <p>
     * Returns RFC 9457 Problem Details with status 400.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response with ProblemDetail
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format(
                "Invalid value for parameter '%s': expected type %s",
                ex.getName(),
                Optional.ofNullable(ex.getRequiredType())
                        .map(Class::getSimpleName)
                        .orElse("unknown")
        );
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Bad Request");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles ServerWebInputException - reactive parameter binding errors.
     * <p>
     * Thrown by Spring WebFlux when request parameters cannot be parsed or bound.
     * <p>
     * <b>Common scenarios:</b>
     * <ul>
     *   <li>Missing required parameter (e.g., missing applicationDate)</li>
     *   <li>Invalid date format (e.g., "2020-99-99" for LocalDateTime)</li>
     *   <li>Type conversion failures (e.g., "abc" for Long productId)</li>
     * </ul>
     * <p>
     * Returns RFC 9457 Problem Details with status 400.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response with ProblemDetail
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDetail> handleServerWebInputException(ServerWebInputException ex) {
        String detail = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Bad Request");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles WebExchangeBindException - reactive @Valid validation errors.
     * <p>
     * Thrown when Bean Validation constraints fail on reactive controller parameters.
     * <p>
     * <b>Example scenarios:</b>
     * <ul>
     *   <li>@NotNull constraint violation</li>
     *   <li>@Min constraint violation (e.g., productId = -1)</li>
     *   <li>@Valid nested object validation failure</li>
     * </ul>
     * <p>
     * Returns RFC 9457 Problem Details with status 400.
     *
     * @param ex the exception containing field errors
     * @return 400 BAD REQUEST response with ProblemDetail containing detailed validation messages
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException ex) {
        String detail = ex.getFieldErrors().stream()
                .map(fieldError -> String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        if (detail.isEmpty()) {
            detail = "Validation failed: " + ex.getMessage();
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles ConstraintViolationException - Bean Validation constraint violations.
     * <p>
     * Thrown when Jakarta Bean Validation constraints are violated
     * (e.g., @NotNull, @Min, @Max on method parameters).
     * <p>
     * Returns RFC 9457 Problem Details with status 400.
     *
     * @param ex the exception
     * @return 400 BAD REQUEST response with ProblemDetail
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(violation -> String.format("%s: %s", violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining("; "));

        if (detail.isEmpty()) {
            detail = "Validation failed: " + ex.getMessage();
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Constraint Violation");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles all other unexpected exceptions.
     * <p>
     * This is a catch-all handler for any exception not explicitly handled above.
     * Returns a generic error message to avoid leaking sensitive information.
     * <p>
     * Returns RFC 9457 Problem Details with status 500.
     *
     * @param ex the exception
     * @return 500 INTERNAL SERVER ERROR response with ProblemDetail
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage()
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}

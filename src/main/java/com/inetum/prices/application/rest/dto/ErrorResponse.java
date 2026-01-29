package com.inetum.prices.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * REST API Error Response DTO.
 * <p>
 * This record represents the JSON error response returned when exceptions occur.
 * It provides consistent error messaging across the REST API.
 * <p>
 * <b>Example JSON Error Response:</b>
 * <pre>
 * {
 *   "status": 404,
 *   "message": "No applicable price found for product 35455, brand 1 at 2020-06-14T10:00:00",
 *   "timestamp": "2026-01-28T12:06:00"
 * }
 * </pre>
 *
 * @param status    HTTP status code
 * @param message   human-readable error message
 * @param timestamp when the error occurred
 */
public record ErrorResponse(
        int status,
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
}

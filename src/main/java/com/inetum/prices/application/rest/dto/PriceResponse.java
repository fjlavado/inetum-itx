package com.inetum.prices.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REST API Response DTO for price queries.
 * <p>
 * This record represents the JSON response body returned by the GET /prices endpoint.
 * It uses Java Records for immutability, conciseness, and automatic JSON serialization.
 * <p>
 * <b>Example JSON Response:</b>
 * <pre>
 * {
 *   "productId": 35455,
 *   "brandId": 1,
 *   "priceList": 1,
 *   "startDate": "2020-06-14T00:00:00",
 *   "endDate": "2020-12-31T23:59:59",
 *   "price": 35.50
 * }
 * </pre>
 *
 * @param productId the product identifier
 * @param brandId   the brand identifier
 * @param priceList the price list identifier
 * @param startDate the start date/time of price validity
 * @param endDate   the end date/time of price validity
 * @param price     the price amount
 */
public record PriceResponse(
        long productId,
        long brandId,
        int priceList,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startDate,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endDate,
        BigDecimal price
) {
}

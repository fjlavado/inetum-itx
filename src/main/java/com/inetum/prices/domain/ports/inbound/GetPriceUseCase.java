package com.inetum.prices.domain.ports.inbound;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Inbound Port (Use Case) for querying applicable prices reactively.
 * <p>
 * This interface defines the contract for the primary use case in the pricing domain:
 * "Get the applicable price for a product and brand at a specific date/time."
 * <p>
 * <b>Hexagonal Architecture:</b> This is a PRIMARY PORT - it represents
 * what the domain offers to the outside world (driving adapters like REST controllers).
 * <p>
 * <b>Design Rationale:</b>
 * <ul>
 *   <li>Single responsibility: one use case per interface</li>
 *   <li>Technology agnostic: no HTTP, JSON, or database concepts</li>
 *   <li>Uses domain value objects for type safety</li>
 *   <li>Returns reactive Mono for non-blocking execution</li>
 *   <li>Errors propagated as Mono.error() signals</li>
 * </ul>
 */
public interface GetPriceUseCase {

    /**
     * Retrieves the applicable price for a product/brand at a specific date/time reactively.
     * <p>
     * <b>Business Logic:</b>
     * <ol>
     *   <li>Find all prices matching brand, product, and date range</li>
     *   <li>If multiple prices found, select the one with the highest priority</li>
     *   <li>If no price found, emit Mono.error(PriceNotFoundException)</li>
     * </ol>
     *
     * @param applicationDate the date/time to query (must not be null)
     * @param productId       the product identifier (must not be null)
     * @param brandId         the brand identifier (must not be null)
     * @return Mono emitting the applicable Price with the highest priority, or error if not found
     * @throws IllegalArgumentException if any parameter is null (emitted as @{@link Mono.Error})
     */
    Mono<Price> getApplicablePrice(LocalDateTime applicationDate, ProductId productId, BrandId brandId);
}

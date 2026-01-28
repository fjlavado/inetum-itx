package com.inetum.prices.domain.service;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import com.inetum.prices.domain.ports.outbound.PriceRepositoryPort;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Domain Service implementing the GetPriceUseCase.
 * <p>
 * This service encapsulates the core business logic for price resolution:
 * finding the applicable price with the highest priority when multiple
 * prices overlap for the same product/brand/date combination.
 * <p>
 * <b>Design Characteristics:</b>
 * <ul>
 *   <li>Pure domain logic - no Spring annotations or framework dependencies</li>
 *   <li>Stateless - all state comes from parameters</li>
 *   <li>Dependency injection through constructor (framework-agnostic)</li>
 *   <li>Immutable once constructed</li>
 * </ul>
 * <p>
 * <b>Business Rules Implemented:</b>
 * <ol>
 *   <li>Query repository for all applicable prices</li>
 *   <li>Filter prices that match the exact date/time (delegated to aggregate)</li>
 *   <li>Select the price with highest priority</li>
 *   <li>Throw exception if no applicable price found</li>
 * </ol>
 */
public class PriceService implements GetPriceUseCase {

    private final PriceRepositoryPort priceRepository;

    /**
     * Constructs a new PriceService with the required dependencies.
     * <p>
     * Note: No Spring @Service or @Component annotations - this is pure domain code.
     * The application layer will register this as a Spring bean via @Configuration.
     *
     * @param priceRepository the repository port for fetching prices
     * @throws IllegalArgumentException if priceRepository is null
     */
    public PriceService(PriceRepositoryPort priceRepository) {
        if (priceRepository == null) {
            throw new IllegalArgumentException("PriceRepository cannot be null");
        }
        this.priceRepository = priceRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation Algorithm:</b>
     * <ol>
     *   <li>Validate input parameters (null checks)</li>
     *   <li>Query repository for candidate prices</li>
     *   <li>Sort by priority (descending) and select the first one</li>
     *   <li>Throw PriceNotFoundException if no price found</li>
     * </ol>
     */
    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, ProductId productId, BrandId brandId) {
        // Validate inputs
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (brandId == null) {
            throw new IllegalArgumentException("BrandId cannot be null");
        }

        // Query repository for applicable prices
        List<Price> applicablePrices = priceRepository.findApplicablePrices(
                applicationDate,
                productId,
                brandId
        );

        // Find the price with highest priority
        // The repository returns all matching prices; we select the one with max priority
        return applicablePrices.stream()
                .max(Comparator.comparing(Price::priority))
                .orElseThrow(() -> new PriceNotFoundException(applicationDate, productId, brandId));
    }
}

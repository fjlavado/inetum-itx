package com.inetum.prices.domain.exception;

import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;

/**
 * Domain exception thrown when no applicable price is found for a given query.
 * <p>
 * This exception is part of the domain layer and represents a business rule violation:
 * "A price must exist for every valid product/brand/date combination in production."
 * <p>
 * This is a checked business exception (extends RuntimeException for convenience)
 * that should be caught and translated to appropriate HTTP responses in the
 * application layer.
 */
public class PriceNotFoundException extends RuntimeException {

    private final LocalDateTime applicationDate;
    private final transient ProductId productId;
    private final transient BrandId brandId;

    /**
     * Constructs a new PriceNotFoundException with detailed context.
     *
     * @param applicationDate the date queried
     * @param productId       the product queried
     * @param brandId         the brand queried
     */
    public PriceNotFoundException(LocalDateTime applicationDate, ProductId productId, BrandId brandId) {
        super(String.format(
                "No applicable price found for product %d, brand %d at %s",
                productId.value(),
                brandId.value(),
                applicationDate
        ));
        this.applicationDate = applicationDate;
        this.productId = productId;
        this.brandId = brandId;
    }

    /**
     * Constructs a new PriceNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public PriceNotFoundException(String message) {
        super(message);
        this.applicationDate = null;
        this.productId = null;
        this.brandId = null;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public ProductId getProductId() {
        return productId;
    }

    public BrandId getBrandId() {
        return brandId;
    }
}

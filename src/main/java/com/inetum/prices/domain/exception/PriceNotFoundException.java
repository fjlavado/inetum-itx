package com.inetum.prices.domain.exception;

import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;

/**
 * Exception thrown when no applicable price is found for the given criteria.
 */
public class PriceNotFoundException extends DomainErrorException {

    public PriceNotFoundException(LocalDateTime applicationDate, ProductId productId, BrandId brandId) {
        super(String.format(
                "No applicable price found for product %d, brand %d at %s",
                productId.value(),
                brandId.value(),
                applicationDate));
    }
}

package com.inetum.prices.domain.service.mapper;

import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.PriceRule;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between PriceRule and Price domain entities.
 * <p>
 * This mapper isolates the conversion logic from PriceService, making it:
 * <ul>
 *   <li>Testable in isolation</li>
 *   <li>Reusable across different services</li>
 *   <li>Maintainable with clear single responsibility</li>
 * </ul>
 * <p>
 * The conversion is necessary to maintain API compatibility with the existing
 * PriceResponse DTO structure while using the CQRS ProductPriceTimeline aggregate internally.
 * <p>
 * MapStruct generates the implementation at compile time with zero runtime overhead.
 */
@Mapper(componentModel = "spring")
public interface PriceDomainMapper {

    /**
     * Converts a PriceRule from the ProductPriceTimeline aggregate to a Price entity.
     * <p>
     * This method enriches the PriceRule with product and brand identifiers that are
     * stored at the aggregate level but needed at the entity level for API responses.
     *
     * @param rule      the pricing rule from the timeline aggregate
     * @param productId the product identifier (stored at aggregate level)
     * @param brandId   the brand identifier (stored at aggregate level)
     * @return a Price entity suitable for REST API responses
     */
    @Mapping(target = "brandId", source = "brandId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "priceListId", source = "rule.priceListId")
    @Mapping(target = "startDate", source = "rule.startDate")
    @Mapping(target = "endDate", source = "rule.endDate")
    @Mapping(target = "priority", source = "rule.priority")
    @Mapping(target = "amount", source = "rule.amount")
    Price toPriceEntity(PriceRule rule, ProductId productId, BrandId brandId);
}

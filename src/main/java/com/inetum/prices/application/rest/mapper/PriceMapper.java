package com.inetum.prices.application.rest.mapper;

import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.domain.model.Price;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mapper for converting between Domain Price and API PriceResponse.
 */
@Mapper(componentModel = "spring")
public interface PriceMapper {

    @Mapping(target = "productId", expression = "java(price.productId().value())")
    @Mapping(target = "brandId", expression = "java(price.brandId().value())")
    @Mapping(target = "priceList", expression = "java(price.priceListId().value())")
    @Mapping(target = "price", expression = "java(price.amount().amount())")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    PriceResponse toResponse(Price price);

    /**
     * Maps LocalDateTime to OffsetDateTime (assuming UTC).
     */
    default OffsetDateTime mapToOffsetDateTime(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}

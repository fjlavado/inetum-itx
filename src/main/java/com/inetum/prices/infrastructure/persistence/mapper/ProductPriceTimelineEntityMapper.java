package com.inetum.prices.infrastructure.persistence.mapper;

import com.inetum.prices.domain.model.ProductPriceTimeline;
import com.inetum.prices.domain.model.valueobject.SingleValueObject;
import com.inetum.prices.infrastructure.persistence.entity.ProductPriceTimelineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

/**
 * MapStruct mapper for converting between ProductPriceTimeline domain model
 * and ProductPriceTimelineEntity JPA entity.
 * <p>
 * This mapper handles the impedance mismatch between:
 * <ul>
 *   <li>Domain layer: ProductId/BrandId value objects</li>
 *   <li>Infrastructure layer: Long primitive types</li>
 * </ul>
 * <p>
 * MapStruct generates the implementation at compile time, ensuring type-safe
 * and performant conversions without reflection.
 */
@Mapper(componentModel = "spring")
public interface ProductPriceTimelineEntityMapper {

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity
     * @return the domain model
     */
    @Mapping(target = "productId.value", source = "entity.productId")
    @Mapping(target = "brandId.value", source = "entity.brandId")
    @Mapping(target = "rules", source = "priceRules")
    ProductPriceTimeline toDomain(ProductPriceTimelineEntity entity);

    /**
     * Converts domain model to JPA entity.
     *
     * @param domain the domain model
     * @return the JPA entity
     */
    @Mapping(target = "productId", source = "domain.productId")
    @Mapping(target = "brandId", source = "domain.brandId")
    @Mapping(target = "priceRules", source = "rules")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductPriceTimelineEntity toEntity(ProductPriceTimeline domain);

    default <T> T mapVo(SingleValueObject<T> vo) {
        return Optional.ofNullable(vo).map(SingleValueObject::value).orElse(null);
    }
}

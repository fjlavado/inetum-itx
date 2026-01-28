package com.inetum.prices.infrastructure.persistence.mapper;

import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.infrastructure.persistence.entity.PriceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting between PriceEntity (infrastructure) and Price (domain).
 * <p>
 * This mapper bridges the infrastructure layer (JPA entities) with the domain layer
 * (domain models and value objects), ensuring proper isolation between layers.
 * <p>
 * <b>Mapping Strategy:</b>
 * <ul>
 *   <li>Primitive types (Long, Integer, BigDecimal) → Value Objects (BrandId, ProductId, Money, etc.)</li>
 *   <li>LocalDateTime fields map directly (no conversion needed)</li>
 *   <li>Uses custom methods for value object creation</li>
 * </ul>
 * <p>
 * MapStruct will generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface PriceEntityMapper {

    /**
     * Maps from PriceEntity to Price domain model.
     * <p>
     * MapStruct will automatically use the helper methods below to map
     * primitive types to value objects.
     *
     * @param entity the JPA entity
     * @return the domain Price aggregate
     */
    @Mapping(target = "brandId", source = "brandId")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "priceListId", source = "priceList")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "amount", source = "price")
    Price toDomain(PriceEntity entity);

    /**
     * Maps a list of PriceEntity to a list of Price domain models.
     *
     * @param entities the list of JPA entities
     * @return the list of domain Price aggregates
     */
    List<Price> toDomainList(List<PriceEntity> entities);

    /**
     * Helper method to map Long to BrandId.
     *
     * @param brandId the primitive brand ID
     * @return BrandId value object
     */
    default BrandId mapToBrandId(Long brandId) {
        return brandId != null ? new BrandId(brandId) : null;
    }

    /**
     * Helper method to map Long to ProductId.
     *
     * @param productId the primitive product ID
     * @return ProductId value object
     */
    default ProductId mapToProductId(Long productId) {
        return productId != null ? new ProductId(productId) : null;
    }

    /**
     * Helper method to map Integer to PriceListId.
     *
     * @param priceList the primitive price list ID
     * @return PriceListId value object
     */
    default PriceListId mapToPriceListId(Integer priceList) {
        return priceList != null ? new PriceListId(priceList) : null;
    }

    /**
     * Helper method to map Integer to Priority.
     *
     * @param priority the primitive priority value
     * @return Priority value object
     */
    default Priority mapToPriority(Integer priority) {
        return priority != null ? new Priority(priority) : null;
    }

    /**
     * Helper method to map BigDecimal to Money.
     *
     * @param price the primitive price amount
     * @return Money value object
     */
    default Money mapToMoney(java.math.BigDecimal price) {
        return price != null ? new Money(price) : null;
    }
}

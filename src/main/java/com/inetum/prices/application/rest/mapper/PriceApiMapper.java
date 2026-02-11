package com.inetum.prices.application.rest.mapper;

import com.inetum.prices.application.rest.dto.generated.PriceResponse;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.model.valueobject.SingleValueObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * MapStruct mapper for bidirectional conversion between domain models and OpenAPI-generated DTOs.
 * <p>
 * This mapper is the boundary between the domain layer (pure Java, framework-agnostic)
 * and the REST layer (OpenAPI-generated DTOs for API contracts).
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Convert domain {@link Price} to {@link PriceResponse} (outbound)</li>
 *   <li>Unwrap value objects to primitives for API responses</li>
 *   <li>Wrap primitives into value objects for domain layer (inbound)</li>
 *   <li>Build error responses with consistent format</li>
 * </ul>
 * <p>
 * <b>Design Notes:</b>
 * <ul>
 *   <li>MapStruct generates the implementation at compile-time (see target/generated-sources/annotations)</li>
 *   <li>Value objects are unwrapped via their accessor methods (.value(), .amount())</li>
 *   <li>BigDecimal from Money is converted to Double for JSON serialization</li>
 *   <li>Component model = "spring" enables dependency injection</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface PriceApiMapper {

    /**
     * Converts a domain Price entity to a PriceResponse DTO for API responses.
     * <p>
     * Maps value objects to their primitive representations:
     * <ul>
     *   <li>ProductId → Long (via mapVo() helper method)</li>
     *   <li>BrandId → Long (via mapVo() helper method)</li>
     *   <li>PriceListId → Long (via mapVo() helper method)</li>
     *   <li>Money → Double (via extractPrice() helper method)</li>
     *   <li>LocalDateTime → LocalDateTime (direct mapping)</li>
     * </ul>
     *
     * @param price the domain Price entity (must not be null)
     * @return the API response DTO
     */
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "brandId", source = "brandId")
    @Mapping(target = "priceList", source = "priceListId")
    @Mapping(target = "price", source = "amount")
    PriceResponse toPriceResponse(Price price);

    /**
     * Extracts the double value from a Money value object.
     * <p>
     * Used by MapStruct when mapping Money to Double for the price field.
     *
     * @param money the Money value object
     * @return the price as Double, or null if money is null
     */
    default Double extractPrice(com.inetum.prices.domain.model.valueobject.Money money) {
        return Optional.ofNullable(money)
                .map(com.inetum.prices.domain.model.valueobject.Money::amount)
                .map(BigDecimal::doubleValue)
                .orElse(null);
    }

    /**
     * Converts a primitive Long to a ProductId value object.
     * <p>
     * Used for inbound parameter conversion (query parameters → domain value objects).
     *
     * @param value the product ID as Long
     * @return ProductId value object
     */
    default ProductId toProductId(Long value) {
        return new ProductId(value);
    }

    /**
     * Converts a primitive Long to a BrandId value object.
     * <p>
     * Used for inbound parameter conversion (query parameters → domain value objects).
     *
     * @param value the brand ID as Long
     * @return BrandId value object
     */
    default BrandId toBrandId(Long value) {
        return new BrandId(value);
    }

    /**
     * Unwraps a SingleValueObject to its primitive value.
     * <p>
     * This method is automatically used by MapStruct when mapping value objects
     * to primitives (e.g., ProductId → Long).
     * <p>
     * <b>Example</b>: When mapping `Price.productId` (ProductId value object) to
     * `PriceResponse.productId` (Long), MapStruct calls this method to extract
     * the Long value.
     *
     * @param vo  the value object to unwrap
     * @param <T> the type of the wrapped value
     * @return the unwrapped primitive value, or null if vo is null
     */
    default <T> T mapVo(SingleValueObject<T> vo) {
        return Optional.ofNullable(vo).map(SingleValueObject::value).orElse(null);
    }
}

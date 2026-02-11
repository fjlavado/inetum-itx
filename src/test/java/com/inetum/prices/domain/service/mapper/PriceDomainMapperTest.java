package com.inetum.prices.domain.service.mapper;

import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.PriceRule;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PriceDomainMapper}.
 * <p>
 * Tests verify the MapStruct-generated mapper correctly converts PriceRule to Price
 * while enriching with product and brand identifiers.
 */
@DisplayName("PriceDomainMapper Tests")
class PriceDomainMapperTest {

    private final PriceDomainMapper mapper = Mappers.getMapper(PriceDomainMapper.class);

    @Test
    @DisplayName("Should convert PriceRule to Price entity with all fields")
    void shouldConvertPriceRuleToPriceEntity() {
        // Given
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.brandId()).isEqualTo(brandId);
        assertThat(result.priceListId()).isEqualTo(rule.priceListId());
        assertThat(result.startDate()).isEqualTo(rule.startDate());
        assertThat(result.endDate()).isEqualTo(rule.endDate());
        assertThat(result.priority()).isEqualTo(rule.priority());
        assertThat(result.amount()).isEqualTo(rule.amount());
    }

    @Test
    @DisplayName("Should preserve normalized BigDecimal precision during conversion")
    void shouldPreserveNormalizedBigDecimalPrecision() {
        // Given - Amount that will be normalized to 2 decimals by Money
        BigDecimal originalAmount = new BigDecimal("35.123456789");
        Money money = new Money(originalAmount);
        // Money normalizes to 2 decimal places: 35.12
        BigDecimal expectedAmount = new BigDecimal("35.12");

        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                money
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then - Money normalizes high precision to 2 decimal places
        assertThat(result.amount().amount()).isEqualByComparingTo(expectedAmount);
    }

    @Test
    @DisplayName("Should handle different priority values correctly")
    void shouldHandleDifferentPriorityValues() {
        // Given - High priority rule
        PriceRule highPriorityRule = new PriceRule(
                new PriceListId(2),
                LocalDateTime.of(2020, 6, 14, 15, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30),
                new Priority(1),
                new Money(new BigDecimal("25.45"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(highPriorityRule, productId, brandId);

        // Then
        assertThat(result.priority().value()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle different price list IDs")
    void shouldHandleDifferentPriceListIds() {
        // Given
        PriceRule rule = new PriceRule(
                new PriceListId(4),
                LocalDateTime.of(2020, 6, 15, 16, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(1),
                new Money(new BigDecimal("38.95"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then
        assertThat(result.priceListId().value()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should handle different product and brand IDs")
    void shouldHandleDifferentProductAndBrandIds() {
        // Given
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        ProductId productId = new ProductId(99999L);
        BrandId brandId = new BrandId(5L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then
        assertThat(result.productId().value()).isEqualTo(99999L);
        assertThat(result.brandId().value()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should handle edge case date ranges")
    void shouldHandleEdgeCaseDateRanges() {
        // Given - Very short date range (1 minute)
        LocalDateTime startDateTime = LocalDateTime.of(2020, 6, 14, 10, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2020, 6, 14, 10, 1);
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                startDateTime,
                endDateTime,
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then
        assertThat(result.startDate()).isEqualTo(startDateTime);
        assertThat(result.endDate()).isEqualTo(endDateTime);
    }

    @Test
    @DisplayName("Should handle zero amount")
    void shouldHandleZeroAmount() {
        // Given
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(BigDecimal.ZERO)
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result = mapper.toPriceEntity(rule, productId, brandId);

        // Then
        assertThat(result.amount().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should create independent Price instance")
    void shouldCreateIndependentPriceInstance() {
        // Given
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result1 = mapper.toPriceEntity(rule, productId, brandId);
        Price result2 = mapper.toPriceEntity(rule, productId, brandId);

        // Then - Should create new instances
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("Should maintain value object equality semantics")
    void shouldMaintainValueObjectEqualitySemantics() {
        // Given
        PriceRule rule1 = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        PriceRule rule2 = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price result1 = mapper.toPriceEntity(rule1, productId, brandId);
        Price result2 = mapper.toPriceEntity(rule2, productId, brandId);

        // Then - Should be equal due to same values
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("Should convert multiple rules maintaining independence")
    void shouldConvertMultipleRulesMaintainingIndependence() {
        // Given - Different rules
        PriceRule rule1 = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        );
        PriceRule rule2 = new PriceRule(
                new PriceListId(2),
                LocalDateTime.of(2020, 6, 14, 15, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30),
                new Priority(1),
                new Money(new BigDecimal("25.45"))
        );
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Price price1 = mapper.toPriceEntity(rule1, productId, brandId);
        Price price2 = mapper.toPriceEntity(rule2, productId, brandId);

        // Then
        assertThat(price1.priceListId().value()).isEqualTo(1);
        assertThat(price2.priceListId().value()).isEqualTo(2);
        assertThat(price1.amount().amount()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(price2.amount().amount()).isEqualByComparingTo(new BigDecimal("25.45"));
    }
}

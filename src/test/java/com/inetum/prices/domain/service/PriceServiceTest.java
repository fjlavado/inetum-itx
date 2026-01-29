package com.inetum.prices.domain.service;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.PriceRule;
import com.inetum.prices.domain.model.ProductPriceTimeline;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.outbound.ProductPriceTimelineRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PriceService with CQRS pattern.
 * <p>
 * These tests focus on domain logic in isolation, using Mockito to mock the repository.
 * No Spring context is loaded - these are fast, pure unit tests.
 * <p>
 * Tests have been updated to use ProductPriceTimeline aggregate instead of individual Price entities.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService Unit Tests (CQRS)")
class PriceServiceTest {

    @Mock
    private ProductPriceTimelineRepositoryPort timelineRepository;

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(timelineRepository);
    }

    @Test
    @DisplayName("Should return the price with highest priority when multiple rules overlap")
    void shouldReturnHighestPriorityPrice() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        ProductPriceTimeline timeline = createTimelineWithMultipleRules(productId, brandId);
        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Optional.of(timeline));

        // When
        Price result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then
        assertThat(result.priceListId().value()).isEqualTo(2);
        assertThat(result.priority().value()).isEqualTo(1);
        assertThat(result.amount().amount()).isEqualByComparingTo(new BigDecimal("25.45"));
        verify(timelineRepository).findByProductAndBrand(productId, brandId);
    }

    @Test
    @DisplayName("Should return single price when only one rule applies")
    void shouldReturnSinglePrice() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        ProductPriceTimeline timeline = createTimelineWithSingleRule(productId, brandId);
        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Optional.of(timeline));

        // When
        Price result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then
        assertThat(result.priceListId().value()).isEqualTo(1);
        assertThat(result.priority().value()).isZero();
        assertThat(result.amount().amount()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when timeline not found")
    void shouldThrowExceptionWhenTimelineNotFound() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(99999L);
        BrandId brandId = new BrandId(1L);

        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() ->
                priceService.getApplicablePrice(applicationDate, productId, brandId))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("No applicable price found");
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when no rules apply for date")
    void shouldThrowExceptionWhenNoRulesApply() {
        // Given - date before any rules apply
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 13, 23, 59);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        ProductPriceTimeline timeline = createTimelineWithSingleRule(productId, brandId);
        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Optional.of(timeline));

        // When / Then
        assertThatThrownBy(() ->
                priceService.getApplicablePrice(applicationDate, productId, brandId))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("No applicable price found");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when applicationDate is null")
    void shouldThrowExceptionWhenApplicationDateIsNull() {
        // Given
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When / Then
        assertThatThrownBy(() ->
                priceService.getApplicablePrice(null, productId, brandId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application date cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when productId is null")
    void shouldThrowExceptionWhenProductIdIsNull() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        BrandId brandId = new BrandId(1L);

        // When / Then
        assertThatThrownBy(() ->
                priceService.getApplicablePrice(applicationDate, null, brandId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductId cannot be null");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when brandId is null")
    void shouldThrowExceptionWhenBrandIdIsNull() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);

        // When / Then
        assertThatThrownBy(() ->
                priceService.getApplicablePrice(applicationDate, productId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BrandId cannot be null");
    }

    // Helper methods

    private ProductPriceTimeline createTimelineWithSingleRule(ProductId productId, BrandId brandId) {
        PriceRule rule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59),
                new Priority(0),
                Money.of(BigDecimal.valueOf(35.50))
        );
        return new ProductPriceTimeline(productId, brandId, Arrays.asList(rule));
    }

    private ProductPriceTimeline createTimelineWithMultipleRules(ProductId productId, BrandId brandId) {
        PriceRule baseRule = new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59),
                new Priority(0),
                Money.of(BigDecimal.valueOf(35.50))
        );

        PriceRule promotionRule = new PriceRule(
                new PriceListId(2),
                LocalDateTime.of(2020, 6, 14, 15, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30),
                new Priority(1),
                Money.of(BigDecimal.valueOf(25.45))
        );

        return new ProductPriceTimeline(productId, brandId, Arrays.asList(baseRule, promotionRule));
    }
}

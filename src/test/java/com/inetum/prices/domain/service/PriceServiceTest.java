package com.inetum.prices.domain.service;

import com.inetum.prices.domain.exception.PriceNotFoundException;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.outbound.PriceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PriceService.
 * <p>
 * These tests focus on domain logic in isolation, using Mockito to mock the repository.
 * No Spring context is loaded - these are fast, pure unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService Unit Tests")
class PriceServiceTest {

    @Mock
    private PriceRepositoryPort priceRepository;

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(priceRepository);
    }

    @Test
    @DisplayName("Should return the price with highest priority when multiple prices exist")
    void shouldReturnHighestPriorityPrice() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        Price lowPriorityPrice = createPrice(brandId, productId, 1, 0, new BigDecimal("35.50"));
        Price highPriorityPrice = createPrice(brandId, productId, 2, 1, new BigDecimal("25.45"));

        when(priceRepository.findApplicablePrices(any(), any(), any()))
                .thenReturn(List.of(lowPriorityPrice, highPriorityPrice));

        // When
        Price result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then
        assertThat(result).isEqualTo(highPriorityPrice);
        assertThat(result.priority().value()).isEqualTo(1);
        assertThat(result.amount().amount()).isEqualByComparingTo(new BigDecimal("25.45"));
        verify(priceRepository).findApplicablePrices(applicationDate, productId, brandId);
    }

    @Test
    @DisplayName("Should return single price when only one exists")
    void shouldReturnSinglePrice() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        Price singlePrice = createPrice(brandId, productId, 1, 0, new BigDecimal("35.50"));

        when(priceRepository.findApplicablePrices(any(), any(), any()))
                .thenReturn(List.of(singlePrice));

        // When
        Price result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then
        assertThat(result).isEqualTo(singlePrice);
        assertThat(result.priceListId().value()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw PriceNotFoundException when no prices exist")
    void shouldThrowExceptionWhenNoPriceFound() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(99999L);
        BrandId brandId = new BrandId(1L);

        when(priceRepository.findApplicablePrices(any(), any(), any()))
                .thenReturn(Collections.emptyList());

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

    @Test
    @DisplayName("Should correctly select highest priority among three prices")
    void shouldSelectHighestPriorityAmongMultiplePrices() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 15, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        Price priority0 = createPrice(brandId, productId, 1, 0, new BigDecimal("35.50"));
        Price priority1 = createPrice(brandId, productId, 3, 1, new BigDecimal("30.50"));
        Price priority2 = createPrice(brandId, productId, 4, 2, new BigDecimal("38.95"));

        when(priceRepository.findApplicablePrices(any(), any(), any()))
                .thenReturn(List.of(priority0, priority1, priority2));

        // When
        Price result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then
        assertThat(result).isEqualTo(priority2);
        assertThat(result.priority().value()).isEqualTo(2);
        assertThat(result.priceListId().value()).isEqualTo(4);
        assertThat(result.amount().amount()).isEqualByComparingTo(new BigDecimal("38.95"));
    }

    // Helper method to create Price objects for testing
    private Price createPrice(BrandId brandId, ProductId productId, int priceListId,
                              int priority, BigDecimal amount) {
        return new Price(
                brandId,
                productId,
                new PriceListId(priceListId),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59),
                new Priority(priority),
                new Money(amount)
        );
    }
}

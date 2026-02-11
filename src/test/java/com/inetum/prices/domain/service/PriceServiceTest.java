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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PriceService with reactive CQRS pattern.
 * <p>
 * These tests focus on domain logic in isolation, using Mockito to mock the repository
 * and StepVerifier to test reactive Mono flows.
 * No Spring context is loaded - these are fast, pure unit tests.
 * <p>
 * Tests use ProductPriceTimeline aggregate with reactive Mono<> return types.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService Unit Tests (Reactive CQRS)")
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
                .thenReturn(Mono.just(timeline));

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then - Use StepVerifier for reactive assertions
        StepVerifier.create(result)
                .assertNext(price -> {
                    assertThat(price.priceListId().value()).isEqualTo(2);
                    assertThat(price.priority().value()).isEqualTo(1);
                    assertThat(price.amount().amount()).isEqualByComparingTo(new BigDecimal("25.45"));
                })
                .verifyComplete();

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
                .thenReturn(Mono.just(timeline));

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then - Use StepVerifier
        StepVerifier.create(result)
                .assertNext(price -> {
                    assertThat(price.priceListId().value()).isEqualTo(1);
                    assertThat(price.priority().value()).isZero();
                    assertThat(price.amount().amount()).isEqualByComparingTo(new BigDecimal("35.50"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should emit error when timeline not found")
    void shouldEmitErrorWhenTimelineNotFound() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(99999L);
        BrandId brandId = new BrandId(1L);

        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Mono.empty());

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then - Use StepVerifier to verify error
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PriceNotFoundException &&
                        throwable.getMessage().contains("No applicable price found"))
                .verify();
    }

    @Test
    @DisplayName("Should emit error when no rules apply for date")
    void shouldEmitErrorWhenNoRulesApply() {
        // Given - date before any rules apply
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 13, 23, 59);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        ProductPriceTimeline timeline = createTimelineWithSingleRule(productId, brandId);
        when(timelineRepository.findByProductAndBrand(any(), any()))
                .thenReturn(Mono.just(timeline));

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, productId, brandId);

        // Then - Use StepVerifier to verify error
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof PriceNotFoundException &&
                        throwable.getMessage().contains("No applicable price found"))
                .verify();
    }

    @Test
    @DisplayName("Should emit error when applicationDate is null")
    void shouldEmitErrorWhenApplicationDateIsNull() {
        // Given
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Mono<Price> result = priceService.getApplicablePrice(null, productId, brandId);

        // Then - Use StepVerifier to verify error
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Application date cannot be null"))
                .verify();
    }

    @Test
    @DisplayName("Should emit error when productId is null")
    void shouldEmitErrorWhenProductIdIsNull() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        BrandId brandId = new BrandId(1L);

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, null, brandId);

        // Then - Use StepVerifier to verify error
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("ProductId cannot be null"))
                .verify();
    }

    @Test
    @DisplayName("Should emit error when brandId is null")
    void shouldEmitErrorWhenBrandIdIsNull() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);

        // When
        Mono<Price> result = priceService.getApplicablePrice(applicationDate, productId, null);

        // Then - Use StepVerifier to verify error
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("BrandId cannot be null"))
                .verify();
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

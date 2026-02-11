package com.inetum.prices.domain.exception;

import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PriceNotFoundException}.
 * <p>
 * Tests verify exception creation with context, reactive error handling, and field accessors.
 */
@DisplayName("PriceNotFoundException Tests")
class PriceNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with full context")
    void shouldCreateExceptionWithFullContext() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        PriceNotFoundException exception = new PriceNotFoundException(applicationDate, productId, brandId);

        // Then
        assertThat(exception).isInstanceOf(DomainErrorException.class);
        assertThat(exception.getMessage()).contains("No applicable price found");
        assertThat(exception.getMessage()).contains("35455");
        assertThat(exception.getMessage()).contains("1");
        assertThat(exception.getMessage()).contains("2020-06-14T10:00");
        assertThat(exception.getApplicationDate()).isEqualTo(applicationDate);
        assertThat(exception.getProductId()).isEqualTo(productId);
        assertThat(exception.getBrandId()).isEqualTo(brandId);
    }

    @Test
    @DisplayName("Should create exception with custom message")
    void shouldCreateExceptionWithCustomMessage() {
        // Given
        String customMessage = "Custom price not found message";

        // When
        PriceNotFoundException exception = new PriceNotFoundException(customMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getApplicationDate()).isNull();
        assertThat(exception.getProductId()).isNull();
        assertThat(exception.getBrandId()).isNull();
    }

    @Test
    @DisplayName("Should format message with correct product and brand IDs")
    void shouldFormatMessageWithCorrectProductAndBrandIds() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 12, 31, 23, 59);
        ProductId productId = new ProductId(99999L);
        BrandId brandId = new BrandId(5L);

        // When
        PriceNotFoundException exception = new PriceNotFoundException(applicationDate, productId, brandId);

        // Then
        assertThat(exception.getMessage()).contains("product 99999");
        assertThat(exception.getMessage()).contains("brand 5");
        assertThat(exception.getMessage()).contains("2020-12-31T23:59");
    }

    @Test
    @DisplayName("Should return reactive Mono error via asError helper")
    void shouldReturnReactiveMonoErrorViaAsErrorHelper() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Mono<Object> errorMono = PriceNotFoundException.asError(applicationDate, productId, brandId);

        // Then - Use StepVerifier to test reactive error
        StepVerifier.create(errorMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof PriceNotFoundException &&
                        throwable.getMessage().contains("No applicable price found") &&
                        throwable.getMessage().contains("35455") &&
                        throwable.getMessage().contains("1"))
                .verify();
    }

    @Test
    @DisplayName("Should create Mono error with correct exception type")
    void shouldCreateMonoErrorWithCorrectExceptionType() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        Mono<String> errorMono = PriceNotFoundException.asError(applicationDate, productId, brandId);

        // Then - Verify exception propagates correctly
        StepVerifier.create(errorMono)
                .expectError(PriceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should preserve exception context in reactive error")
    void shouldPreserveExceptionContextInReactiveError() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 30);
        ProductId productId = new ProductId(12345L);
        BrandId brandId = new BrandId(3L);

        // When
        Mono<Object> errorMono = PriceNotFoundException.asError(applicationDate, productId, brandId);

        // Then - Verify context is preserved
        StepVerifier.create(errorMono)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(PriceNotFoundException.class);
                    PriceNotFoundException exception = (PriceNotFoundException) throwable;
                    assertThat(exception.getApplicationDate()).isEqualTo(applicationDate);
                    assertThat(exception.getProductId()).isEqualTo(productId);
                    assertThat(exception.getBrandId()).isEqualTo(brandId);
                })
                .verify();
    }

    @Test
    @DisplayName("Should have correct inheritance hierarchy")
    void shouldHaveCorrectInheritanceHierarchy() {
        // Given
        PriceNotFoundException exception = new PriceNotFoundException("test");

        // Then
        assertThat(exception).isInstanceOf(DomainErrorException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle different date formats in message")
    void shouldHandleDifferentDateFormatsInMessage() {
        // Given - Test with different dates
        LocalDateTime date1 = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        PriceNotFoundException exception1 = new PriceNotFoundException(date1, productId, brandId);
        PriceNotFoundException exception2 = new PriceNotFoundException(date2, productId, brandId);

        // Then
        assertThat(exception1.getMessage()).contains("2020-01-01T00:00");
        assertThat(exception2.getMessage()).contains("2020-12-31T23:59:59");
    }

    @Test
    @DisplayName("Should maintain field immutability via getters")
    void shouldMaintainFieldImmutabilityViaGetters() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When
        PriceNotFoundException exception = new PriceNotFoundException(applicationDate, productId, brandId);

        // Then - Getters should return same values
        assertThat(exception.getApplicationDate()).isEqualTo(applicationDate);
        assertThat(exception.getProductId()).isEqualTo(productId);
        assertThat(exception.getBrandId()).isEqualTo(brandId);
        // Verify immutability by checking multiple calls
        assertThat(exception.getApplicationDate()).isSameAs(exception.getApplicationDate());
        assertThat(exception.getProductId()).isSameAs(exception.getProductId());
        assertThat(exception.getBrandId()).isSameAs(exception.getBrandId());
    }

    @Test
    @DisplayName("Should work in reactive chain with switchIfEmpty")
    void shouldWorkInReactiveChainWithSwitchIfEmpty() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When - Simulate typical usage in service layer
        Mono<String> result = Mono.<String>empty()
                .switchIfEmpty(PriceNotFoundException.asError(applicationDate, productId, brandId));

        // Then
        StepVerifier.create(result)
                .expectError(PriceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should support different generic types in asError")
    void shouldSupportDifferentGenericTypesInAsError() {
        // Given
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0);
        ProductId productId = new ProductId(35455L);
        BrandId brandId = new BrandId(1L);

        // When - Test with different return types
        Mono<String> stringError = PriceNotFoundException.asError(applicationDate, productId, brandId);
        Mono<Integer> intError = PriceNotFoundException.asError(applicationDate, productId, brandId);
        Mono<Object> objectError = PriceNotFoundException.asError(applicationDate, productId, brandId);

        // Then - All should produce same exception type
        StepVerifier.create(stringError).expectError(PriceNotFoundException.class).verify();
        StepVerifier.create(intError).expectError(PriceNotFoundException.class).verify();
        StepVerifier.create(objectError).expectError(PriceNotFoundException.class).verify();
    }
}

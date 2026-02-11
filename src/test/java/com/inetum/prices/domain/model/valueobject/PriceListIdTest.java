package com.inetum.prices.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PriceListId} value object.
 * <p>
 * Tests verify validation, factory methods, and record behavior.
 */
@DisplayName("PriceListId Value Object Tests")
class PriceListIdTest {

    @Test
    @DisplayName("Should create PriceListId with valid value")
    void shouldCreatePriceListIdWithValidValue() {
        // When
        PriceListId priceListId = new PriceListId(1);

        // Then
        assertThat(priceListId.value()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create PriceListId using of() factory method")
    void shouldCreatePriceListIdUsingOfFactoryMethod() {
        // When
        PriceListId priceListId = PriceListId.of(3);

        // Then
        assertThat(priceListId.value()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        // When/Then
        assertThatThrownBy(() -> new PriceListId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PriceListId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when value is zero")
    void shouldThrowExceptionWhenValueIsZero() {
        // When/Then
        assertThatThrownBy(() -> new PriceListId(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PriceListId must be positive");
    }

    @Test
    @DisplayName("Should throw exception when value is negative")
    void shouldThrowExceptionWhenValueIsNegative() {
        // When/Then
        assertThatThrownBy(() -> new PriceListId(-25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PriceListId must be positive")
                .hasMessageContaining("-25");
    }

    @Test
    @DisplayName("Should be equal when values are equal")
    void shouldBeEqualWhenValuesAreEqual() {
        // Given
        PriceListId priceListId1 = PriceListId.of(4);
        PriceListId priceListId2 = PriceListId.of(4);

        // When/Then
        assertThat(priceListId1).isEqualTo(priceListId2);
        assertThat(priceListId1.hashCode()).isEqualTo(priceListId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        // Given
        PriceListId priceListId1 = PriceListId.of(1);
        PriceListId priceListId2 = PriceListId.of(2);

        // When/Then
        assertThat(priceListId1).isNotEqualTo(priceListId2);
    }

    @Test
    @DisplayName("Should return string representation with value")
    void shouldReturnStringRepresentationWithValue() {
        // Given
        PriceListId priceListId = PriceListId.of(4);

        // When
        String result = priceListId.toString();

        // Then
        assertThat(result).contains("4");
    }
}

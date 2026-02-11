package com.inetum.prices.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ProductId} value object.
 * <p>
 * Tests verify validation, factory methods, and record behavior.
 */
@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

    @Test
    @DisplayName("Should create ProductId with valid value")
    void shouldCreateProductIdWithValidValue() {
        // When
        ProductId productId = new ProductId(35455L);

        // Then
        assertThat(productId.value()).isEqualTo(35455L);
    }

    @Test
    @DisplayName("Should create ProductId using of() factory method")
    void shouldCreateProductIdUsingOfFactoryMethod() {
        // When
        ProductId productId = ProductId.of(12345L);

        // Then
        assertThat(productId.value()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        // When/Then
        assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when value is zero")
    void shouldThrowExceptionWhenValueIsZero() {
        // When/Then
        assertThatThrownBy(() -> new ProductId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductId must be positive");
    }

    @Test
    @DisplayName("Should throw exception when value is negative")
    void shouldThrowExceptionWhenValueIsNegative() {
        // When/Then
        assertThatThrownBy(() -> new ProductId(-100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductId must be positive")
                .hasMessageContaining("-100");
    }

    @Test
    @DisplayName("Should be equal when values are equal")
    void shouldBeEqualWhenValuesAreEqual() {
        // Given
        ProductId productId1 = ProductId.of(35455L);
        ProductId productId2 = ProductId.of(35455L);

        // When/Then
        assertThat(productId1).isEqualTo(productId2);
        assertThat(productId1.hashCode()).isEqualTo(productId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        // Given
        ProductId productId1 = ProductId.of(35455L);
        ProductId productId2 = ProductId.of(12345L);

        // When/Then
        assertThat(productId1).isNotEqualTo(productId2);
    }

    @Test
    @DisplayName("Should return string representation with value")
    void shouldReturnStringRepresentationWithValue() {
        // Given
        ProductId productId = ProductId.of(35455L);

        // When
        String result = productId.toString();

        // Then
        assertThat(result).contains("35455");
    }
}

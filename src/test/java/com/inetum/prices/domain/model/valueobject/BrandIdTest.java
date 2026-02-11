package com.inetum.prices.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BrandId} value object.
 * <p>
 * Tests verify validation, factory methods, and record behavior.
 */
@DisplayName("BrandId Value Object Tests")
class BrandIdTest {

    @Test
    @DisplayName("Should create BrandId with valid value")
    void shouldCreateBrandIdWithValidValue() {
        // When
        BrandId brandId = new BrandId(1L);

        // Then
        assertThat(brandId.value()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should create BrandId using of() factory method")
    void shouldCreateBrandIdUsingOfFactoryMethod() {
        // When
        BrandId brandId = BrandId.of(2L);

        // Then
        assertThat(brandId.value()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        // When/Then
        assertThatThrownBy(() -> new BrandId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BrandId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when value is zero")
    void shouldThrowExceptionWhenValueIsZero() {
        // When/Then
        assertThatThrownBy(() -> new BrandId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BrandId must be positive");
    }

    @Test
    @DisplayName("Should throw exception when value is negative")
    void shouldThrowExceptionWhenValueIsNegative() {
        // When/Then
        assertThatThrownBy(() -> new BrandId(-50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BrandId must be positive")
                .hasMessageContaining("-50");
    }

    @Test
    @DisplayName("Should be equal when values are equal")
    void shouldBeEqualWhenValuesAreEqual() {
        // Given
        BrandId brandId1 = BrandId.of(1L);
        BrandId brandId2 = BrandId.of(1L);

        // When/Then
        assertThat(brandId1).isEqualTo(brandId2);
        assertThat(brandId1.hashCode()).isEqualTo(brandId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        // Given
        BrandId brandId1 = BrandId.of(1L);
        BrandId brandId2 = BrandId.of(2L);

        // When/Then
        assertThat(brandId1).isNotEqualTo(brandId2);
    }

    @Test
    @DisplayName("Should return string representation with value")
    void shouldReturnStringRepresentationWithValue() {
        // Given
        BrandId brandId = BrandId.of(1L);

        // When
        String result = brandId.toString();

        // Then
        assertThat(result).contains("1");
    }
}

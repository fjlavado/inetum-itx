package com.inetum.prices.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Money} value object.
 * <p>
 * Tests verify validation, comparison, and factory methods.
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money with valid BigDecimal amount")
    void shouldCreateMoneyWithValidBigDecimal() {
        // When
        Money money = new Money(new BigDecimal("35.50"));

        // Then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(money.value()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    @DisplayName("Should create Money using of() factory method with BigDecimal")
    void shouldCreateMoneyUsingOfFactoryMethod() {
        // When
        Money money = Money.of(new BigDecimal("25.45"));

        // Then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("25.45"));
    }

    @Test
    @DisplayName("Should create Money using of() factory method with double")
    void shouldCreateMoneyUsingOfFactoryMethodWithDouble() {
        // When
        Money money = Money.of(30.50);

        // Then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("30.50"));
    }

    @Test
    @DisplayName("Should normalize amount to 2 decimal places")
    void shouldNormalizeAmountTo2DecimalPlaces() {
        // When
        Money money = new Money(new BigDecimal("35.5"));

        // Then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should round amount to 2 decimal places using HALF_UP")
    void shouldRoundAmountHalfUp() {
        // When
        Money money1 = new Money(new BigDecimal("35.555")); // rounds to 35.56
        Money money2 = new Money(new BigDecimal("35.554")); // rounds to 35.55

        // Then
        assertThat(money1.amount()).isEqualByComparingTo(new BigDecimal("35.56"));
        assertThat(money2.amount()).isEqualByComparingTo(new BigDecimal("35.55"));
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        // When/Then
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Money amount cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        // When/Then
        assertThatThrownBy(() -> new Money(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Money amount cannot be negative");
    }

    @Test
    @DisplayName("Should allow zero amount")
    void shouldAllowZeroAmount() {
        // When
        Money money = new Money(BigDecimal.ZERO);

        // Then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should compare Money amounts correctly")
    void shouldCompareMoneyAmountsCorrectly() {
        // Given
        Money smaller = Money.of(new BigDecimal("25.00"));
        Money larger = Money.of(new BigDecimal("35.00"));
        Money equal = Money.of(new BigDecimal("25.00"));

        // When/Then
        assertThat(smaller.compareTo(larger)).isNegative();
        assertThat(larger.compareTo(smaller)).isPositive();
        assertThat(smaller.compareTo(equal)).isZero();
    }

    @Test
    @DisplayName("Should check if Money is greater than another")
    void shouldCheckIfGreaterThan() {
        // Given
        Money smaller = Money.of(new BigDecimal("25.00"));
        Money larger = Money.of(new BigDecimal("35.00"));

        // When/Then
        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
        assertThat(smaller.isGreaterThan(smaller)).isFalse(); // equal amounts
    }

    @Test
    @DisplayName("Should check if Money is less than another")
    void shouldCheckIfLessThan() {
        // Given
        Money smaller = Money.of(new BigDecimal("25.00"));
        Money larger = Money.of(new BigDecimal("35.00"));

        // When/Then
        assertThat(smaller.isLessThan(larger)).isTrue();
        assertThat(larger.isLessThan(smaller)).isFalse();
        assertThat(smaller.isLessThan(smaller)).isFalse(); // equal amounts
    }

    @Test
    @DisplayName("Should return plain string representation")
    void shouldReturnPlainStringRepresentation() {
        // Given
        Money money = Money.of(new BigDecimal("35.50"));

        // When
        String result = money.toString();

        // Then
        assertThat(result).isEqualTo("35.50");
    }

    @Test
    @DisplayName("Should have value() and amount() return same value")
    void shouldHaveValueAndAmountReturnSameValue() {
        // Given
        Money money = Money.of(new BigDecimal("25.45"));

        // When/Then
        assertThat(money.value()).isEqualTo(money.amount());
    }

    @Test
    @DisplayName("Should be equal when amounts are equal")
    void shouldBeEqualWhenAmountsAreEqual() {
        // Given
        Money money1 = Money.of(new BigDecimal("35.50"));
        Money money2 = Money.of(new BigDecimal("35.50"));

        // When/Then
        assertThat(money1).isEqualTo(money2);
        assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when amounts differ")
    void shouldNotBeEqualWhenAmountsDiffer() {
        // Given
        Money money1 = Money.of(new BigDecimal("35.50"));
        Money money2 = Money.of(new BigDecimal("25.45"));

        // When/Then
        assertThat(money1).isNotEqualTo(money2);
    }
}

package com.inetum.prices.domain.model;

import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PriceRule value object.
 */
class PriceRuleTest {

    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 12, 31, 23, 59);

    @Test
    void shouldCreateValidPriceRule() {
        // Given
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));

        // When
        PriceRule rule = new PriceRule(priceListId, START_DATE, END_DATE, priority, amount);

        // Then
        assertNotNull(rule);
        assertEquals(priceListId, rule.priceListId());
        assertEquals(START_DATE, rule.startDate());
        assertEquals(END_DATE, rule.endDate());
        assertEquals(priority, rule.priority());
        assertEquals(amount, rule.amount());
    }

    @Test
    void shouldRejectNullPriceListId() {
        // Given
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(null, START_DATE, END_DATE, priority, amount)
        );
    }

    @Test
    void shouldRejectNullStartDate() {
        // Given
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, null, END_DATE, priority, amount)
        );
    }

    @Test
    void shouldRejectNullEndDate() {
        // Given
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, START_DATE, null, priority, amount)
        );
    }

    @Test
    void shouldRejectNullPriority() {
        // Given
        PriceListId priceListId = new PriceListId(1);
        Money amount = Money.of(BigDecimal.valueOf(35.50));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, START_DATE, END_DATE, null, amount)
        );
    }

    @Test
    void shouldRejectNullAmount() {
        // Given
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, START_DATE, END_DATE, priority, null)
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {
        // Given - start date AFTER end date
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));
        LocalDateTime invalidStartDate = LocalDateTime.of(2020, 12, 31, 23, 59);
        LocalDateTime invalidEndDate = LocalDateTime.of(2020, 6, 14, 0, 0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, invalidStartDate, invalidEndDate, priority, amount)
        );
    }

    @Test
    void shouldRejectEqualStartAndEndDates() {
        // Given - start date EQUALS end date
        PriceListId priceListId = new PriceListId(1);
        Priority priority = new Priority(0);
        Money amount = Money.of(BigDecimal.valueOf(35.50));
        LocalDateTime sameDate = LocalDateTime.of(2020, 6, 14, 10, 0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new PriceRule(priceListId, sameDate, sameDate, priority, amount)
        );
    }

    @Test
    void shouldDetermineApplicabilityCorrectly_withinRange() {
        // Given
        PriceRule rule = createValidPriceRule();
        LocalDateTime applicationDate = LocalDateTime.of(2020, 8, 15, 12, 0);

        // When
        boolean applicable = rule.isApplicableAt(applicationDate);

        // Then
        assertTrue(applicable, "Date within range should be applicable");
    }

    @Test
    void shouldDetermineApplicabilityCorrectly_beforeRange() {
        // Given
        PriceRule rule = createValidPriceRule();
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 13, 23, 59);

        // When
        boolean applicable = rule.isApplicableAt(applicationDate);

        // Then
        assertFalse(applicable, "Date before range should not be applicable");
    }

    @Test
    void shouldDetermineApplicabilityCorrectly_afterRange() {
        // Given
        PriceRule rule = createValidPriceRule();
        LocalDateTime applicationDate = LocalDateTime.of(2021, 1, 1, 0, 0);

        // When
        boolean applicable = rule.isApplicableAt(applicationDate);

        // Then
        assertFalse(applicable, "Date after range should not be applicable");
    }

    @Test
    void shouldHandleEdgeCase_startDateEqualsApplicationDate() {
        // Given
        PriceRule rule = createValidPriceRule();
        LocalDateTime applicationDate = START_DATE; // Exactly at start

        // When
        boolean applicable = rule.isApplicableAt(applicationDate);

        // Then
        assertTrue(applicable, "Start date should be inclusive");
    }

    @Test
    void shouldHandleEdgeCase_endDateEqualsApplicationDate() {
        // Given
        PriceRule rule = createValidPriceRule();
        LocalDateTime applicationDate = END_DATE; // Exactly at end

        // When
        boolean applicable = rule.isApplicableAt(applicationDate);

        // Then
        assertTrue(applicable, "End date should be inclusive");
    }

    @Test
    void shouldRejectNullApplicationDate() {
        // Given
        PriceRule rule = createValidPriceRule();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                rule.isApplicableAt(null)
        );
    }

    @Test
    void shouldComparePriorities_higherPriority() {
        // Given
        PriceRule lowPriorityRule = createPriceRuleWithPriority(0);
        PriceRule highPriorityRule = createPriceRuleWithPriority(1);

        // When & Then
        assertTrue(highPriorityRule.hasHigherPriorityThan(lowPriorityRule));
        assertFalse(lowPriorityRule.hasHigherPriorityThan(highPriorityRule));
    }

    @Test
    void shouldComparePriorities_lowerPriority() {
        // Given
        PriceRule lowPriorityRule = createPriceRuleWithPriority(0);
        PriceRule highPriorityRule = createPriceRuleWithPriority(1);

        // When & Then
        assertTrue(lowPriorityRule.hasLowerPriorityThan(highPriorityRule));
        assertFalse(highPriorityRule.hasLowerPriorityThan(lowPriorityRule));
    }

    @Test
    void shouldRejectNullPriorityComparison_higher() {
        // Given
        PriceRule rule = createValidPriceRule();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                rule.hasHigherPriorityThan(null)
        );
    }

    @Test
    void shouldRejectNullPriorityComparison_lower() {
        // Given
        PriceRule rule = createValidPriceRule();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                rule.hasLowerPriorityThan(null)
        );
    }

    // Helper methods

    private PriceRule createValidPriceRule() {
        return new PriceRule(
                new PriceListId(1),
                START_DATE,
                END_DATE,
                new Priority(0),
                Money.of(BigDecimal.valueOf(35.50))
        );
    }

    private PriceRule createPriceRuleWithPriority(int priorityValue) {
        return new PriceRule(
                new PriceListId(1),
                START_DATE,
                END_DATE,
                new Priority(priorityValue),
                Money.of(BigDecimal.valueOf(35.50))
        );
    }
}

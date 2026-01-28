package com.inetum.prices.domain.model;

import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import com.inetum.prices.domain.model.valueobject.ProductId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductPriceTimeline aggregate.
 */
class ProductPriceTimelineTest {

    private static final ProductId PRODUCT_ID = new ProductId(35455L);
    private static final BrandId BRAND_ID = new BrandId(1L);

    @Test
    void shouldCreateValidProductPriceTimeline() {
        // Given
        List<PriceRule> rules = createSampleRules();

        // When
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // Then
        assertNotNull(timeline);
        assertEquals(PRODUCT_ID, timeline.getProductId());
        assertEquals(BRAND_ID, timeline.getBrandId());
        assertEquals(4, timeline.getRuleCount());
    }

    @Test
    void shouldRejectNullProductId() {
        // Given
        List<PriceRule> rules = createSampleRules();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new ProductPriceTimeline(null, BRAND_ID, rules)
        );
    }

    @Test
    void shouldRejectNullBrandId() {
        // Given
        List<PriceRule> rules = createSampleRules();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new ProductPriceTimeline(PRODUCT_ID, null, rules)
        );
    }

    @Test
    void shouldRejectNullRules() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, null)
        );
    }

    @Test
    void shouldRejectEmptyRules() {
        // Given
        List<PriceRule> emptyRules = Collections.emptyList();

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, emptyRules)
        );
    }

    @Test
    void shouldReturnHighestPriorityWhenMultipleRulesApply() {
        // Given - Test scenario 2: Day 14 at 16:00
        // Both price list 1 (priority 0) and price list 2 (priority 1) apply
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);

        // When
        Optional<PriceRule> effectivePrice = timeline.getEffectivePrice(applicationDate);

        // Then
        assertTrue(effectivePrice.isPresent());
        assertEquals(new PriceListId(2), effectivePrice.get().priceListId());
        assertEquals(new Priority(1), effectivePrice.get().priority());
        assertEquals(Money.of(BigDecimal.valueOf(25.45)), effectivePrice.get().amount());
    }

    @Test
    void shouldReturnEmptyWhenNoRulesApply() {
        // Given - Date before any rules apply
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 13, 23, 59);

        // When
        Optional<PriceRule> effectivePrice = timeline.getEffectivePrice(applicationDate);

        // Then
        assertFalse(effectivePrice.isPresent());
    }

    @Test
    void shouldHandleSingleRule() {
        // Given
        PriceRule singleRule = createPriceRule(1, 0,
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59),
                BigDecimal.valueOf(35.50));
        List<PriceRule> rules = Collections.singletonList(singleRule);
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // When
        Optional<PriceRule> effectivePrice = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 8, 15, 12, 0)
        );

        // Then
        assertTrue(effectivePrice.isPresent());
        assertEquals(singleRule, effectivePrice.get());
    }

    @Test
    void shouldFilterByDateCorrectly() {
        // Given - Test scenario 3: Day 14 at 21:00
        // Only price list 1 applies (price list 2 ended at 18:30)
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 21, 0);

        // When
        Optional<PriceRule> effectivePrice = timeline.getEffectivePrice(applicationDate);

        // Then
        assertTrue(effectivePrice.isPresent());
        assertEquals(new PriceListId(1), effectivePrice.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(35.50)), effectivePrice.get().amount());
    }

    @Test
    void shouldComparePrioritiesCorrectly() {
        // Given - Multiple overlapping rules with different priorities
        List<PriceRule> rules = Arrays.asList(
                createPriceRule(1, 0,
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59),
                        BigDecimal.valueOf(35.50)),
                createPriceRule(2, 1,
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59),
                        BigDecimal.valueOf(30.50)),
                createPriceRule(3, 2,
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59),
                        BigDecimal.valueOf(25.45))
        );
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // When - All three rules apply
        Optional<PriceRule> effectivePrice = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 8, 15, 12, 0)
        );

        // Then - Should return priority 2 (highest)
        assertTrue(effectivePrice.isPresent());
        assertEquals(new PriceListId(3), effectivePrice.get().priceListId());
        assertEquals(new Priority(2), effectivePrice.get().priority());
    }

    @Test
    void shouldHandle5MandatoryScenariosLogic() {
        // Given - All 4 price rules from test data
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // Scenario 1: Day 14 at 10:00 -> Price List 1 (35.50)
        Optional<PriceRule> result1 = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 6, 14, 10, 0)
        );
        assertTrue(result1.isPresent());
        assertEquals(new PriceListId(1), result1.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(35.50)), result1.get().amount());

        // Scenario 2: Day 14 at 16:00 -> Price List 2 (25.45)
        Optional<PriceRule> result2 = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 6, 14, 16, 0)
        );
        assertTrue(result2.isPresent());
        assertEquals(new PriceListId(2), result2.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(25.45)), result2.get().amount());

        // Scenario 3: Day 14 at 21:00 -> Price List 1 (35.50)
        Optional<PriceRule> result3 = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 6, 14, 21, 0)
        );
        assertTrue(result3.isPresent());
        assertEquals(new PriceListId(1), result3.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(35.50)), result3.get().amount());

        // Scenario 4: Day 15 at 10:00 -> Price List 3 (30.50)
        Optional<PriceRule> result4 = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 6, 15, 10, 0)
        );
        assertTrue(result4.isPresent());
        assertEquals(new PriceListId(3), result4.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(30.50)), result4.get().amount());

        // Scenario 5: Day 16 at 21:00 -> Price List 4 (38.95)
        Optional<PriceRule> result5 = timeline.getEffectivePrice(
                LocalDateTime.of(2020, 6, 16, 21, 0)
        );
        assertTrue(result5.isPresent());
        assertEquals(new PriceListId(4), result5.get().priceListId());
        assertEquals(Money.of(BigDecimal.valueOf(38.95)), result5.get().amount());
    }

    @Test
    void shouldRejectNullApplicationDate() {
        // Given
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                timeline.getEffectivePrice(null)
        );
    }

    @Test
    void shouldMatchProductAndBrand() {
        // Given
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // When & Then
        assertTrue(timeline.matchesProductAndBrand(PRODUCT_ID, BRAND_ID));
        assertFalse(timeline.matchesProductAndBrand(new ProductId(99999L), BRAND_ID));
        assertFalse(timeline.matchesProductAndBrand(PRODUCT_ID, new BrandId(99L)));
    }

    @Test
    void shouldReturnUnmodifiableRulesList() {
        // Given
        List<PriceRule> rules = createSampleRules();
        ProductPriceTimeline timeline = new ProductPriceTimeline(PRODUCT_ID, BRAND_ID, rules);

        // When
        List<PriceRule> retrievedRules = timeline.getRules();

        // Then - Should throw exception when trying to modify
        assertThrows(UnsupportedOperationException.class, () ->
                retrievedRules.add(createPriceRule(99, 99,
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                        BigDecimal.valueOf(100.00)))
        );
    }

    // Helper methods

    /**
     * Creates the 4 sample price rules matching V2__insert_test_data.sql
     */
    private List<PriceRule> createSampleRules() {
        return Arrays.asList(
                // Price List 1: Base price (priority 0)
                createPriceRule(1, 0,
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59),
                        BigDecimal.valueOf(35.50)),

                // Price List 2: Afternoon promotion (priority 1)
                createPriceRule(2, 1,
                        LocalDateTime.of(2020, 6, 14, 15, 0),
                        LocalDateTime.of(2020, 6, 14, 18, 30),
                        BigDecimal.valueOf(25.45)),

                // Price List 3: Morning promotion (priority 1)
                createPriceRule(3, 1,
                        LocalDateTime.of(2020, 6, 15, 0, 0),
                        LocalDateTime.of(2020, 6, 15, 11, 0),
                        BigDecimal.valueOf(30.50)),

                // Price List 4: Premium price (priority 1)
                createPriceRule(4, 1,
                        LocalDateTime.of(2020, 6, 15, 16, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59),
                        BigDecimal.valueOf(38.95))
        );
    }

    private PriceRule createPriceRule(int priceListId, int priority,
                                      LocalDateTime startDate, LocalDateTime endDate,
                                      BigDecimal amount) {
        return new PriceRule(
                new PriceListId(priceListId),
                startDate,
                endDate,
                new Priority(priority),
                Money.of(amount)
        );
    }
}

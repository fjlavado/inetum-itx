package com.inetum.prices.infrastructure.persistence.converter;

import com.inetum.prices.domain.model.PriceRule;
import com.inetum.prices.domain.model.valueobject.Money;
import com.inetum.prices.domain.model.valueobject.PriceListId;
import com.inetum.prices.domain.model.valueobject.Priority;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PriceRulesR2dbcConverter} nested converters.
 * <p>
 * Tests verify JSON serialization/deserialization, null handling, and error cases.
 */
@DisplayName("PriceRulesR2dbcConverter Tests")
class PriceRulesR2dbcConverterTest {

    private final PriceRulesR2dbcConverter.PriceRulesToJsonConverter toJsonConverter =
            new PriceRulesR2dbcConverter.PriceRulesToJsonConverter();

    private final PriceRulesR2dbcConverter.JsonToPriceRulesConverter fromJsonConverter =
            new PriceRulesR2dbcConverter.JsonToPriceRulesConverter();

    @Test
    @DisplayName("Should convert valid PriceRules list to JSON")
    void shouldConvertPriceRulesToJson() {
        // Given
        List<PriceRule> rules = List.of(
                new PriceRule(
                        new PriceListId(1),
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                        new Priority(0),
                        new Money(new BigDecimal("35.50"))
                )
        );

        // When
        Json result = toJsonConverter.convert(rules);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.asString()).contains("\"priceListId\"");
        assertThat(result.asString()).contains("35.50");
        assertThat(result.asString()).contains("2020-06-14T00:00:00");
        // Value objects are serialized with their structure
        assertThat(result.asString()).contains("\"value\"");
    }

    @Test
    @DisplayName("Should convert null PriceRules list to empty JSON array")
    void shouldConvertNullToEmptyJsonArray() {
        // When
        Json result = toJsonConverter.convert(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.asString()).isEqualTo("[]");
    }

    @Test
    @DisplayName("Should convert empty PriceRules list to empty JSON array")
    void shouldConvertEmptyListToEmptyJsonArray() {
        // Given
        List<PriceRule> emptyList = List.of();

        // When
        Json result = toJsonConverter.convert(emptyList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.asString()).isEqualTo("[]");
    }

    @Test
    @DisplayName("Should convert multiple PriceRules to JSON array")
    void shouldConvertMultiplePriceRulesToJsonArray() {
        // Given
        List<PriceRule> rules = List.of(
                new PriceRule(
                        new PriceListId(1),
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                        new Priority(0),
                        new Money(new BigDecimal("35.50"))
                ),
                new PriceRule(
                        new PriceListId(2),
                        LocalDateTime.of(2020, 6, 14, 15, 0),
                        LocalDateTime.of(2020, 6, 14, 18, 30),
                        new Priority(1),
                        new Money(new BigDecimal("25.45"))
                )
        );

        // When
        Json result = toJsonConverter.convert(rules);

        // Then - Value objects are serialized with nested structure
        assertThat(result.asString()).contains("\"priceListId\"");
        assertThat(result.asString()).contains("\"value\":1");
        assertThat(result.asString()).contains("\"value\":2");
        assertThat(result.asString()).contains("35.50");
        assertThat(result.asString()).contains("25.45");
    }

    @Test
    @DisplayName("Should convert JSON to PriceRules list")
    void shouldConvertJsonToPriceRulesList() {
        // Given - JSON with value object structure
        String jsonString = """
                [
                    {
                        "priceListId": {"value": 1},
                        "startDate": "2020-06-14T00:00:00",
                        "endDate": "2020-12-31T23:59:59",
                        "priority": {"value": 0},
                        "amount": {"amount": 35.50}
                    }
                ]
                """;
        Json json = Json.of(jsonString);

        // When
        List<PriceRule> result = fromJsonConverter.convert(json);

        // Then
        assertThat(result).hasSize(1);
        PriceRule rule = result.get(0);
        assertThat(rule.priceListId().value()).isEqualTo(1);
        assertThat(rule.startDate()).isEqualTo(LocalDateTime.of(2020, 6, 14, 0, 0));
        assertThat(rule.endDate()).isEqualTo(LocalDateTime.of(2020, 12, 31, 23, 59, 59));
        assertThat(rule.priority().value()).isEqualTo(0);
        assertThat(rule.amount().amount()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    @DisplayName("Should convert null JSON to empty list")
    void shouldConvertNullJsonToEmptyList() {
        // When
        List<PriceRule> result = fromJsonConverter.convert(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should convert empty JSON string to empty list")
    void shouldConvertEmptyJsonStringToEmptyList() {
        // Given
        Json json = Json.of("   ");

        // When
        List<PriceRule> result = fromJsonConverter.convert(json);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should convert empty JSON array to empty list")
    void shouldConvertEmptyJsonArrayToEmptyList() {
        // Given
        Json json = Json.of("[]");

        // When
        List<PriceRule> result = fromJsonConverter.convert(json);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should convert JSON with multiple rules to list")
    void shouldConvertJsonWithMultipleRulesToList() {
        // Given - JSON with value object structure
        String jsonString = """
                [
                    {
                        "priceListId": {"value": 1},
                        "startDate": "2020-06-14T00:00:00",
                        "endDate": "2020-12-31T23:59:59",
                        "priority": {"value": 0},
                        "amount": {"amount": 35.50}
                    },
                    {
                        "priceListId": {"value": 2},
                        "startDate": "2020-06-14T15:00:00",
                        "endDate": "2020-06-14T18:30:00",
                        "priority": {"value": 1},
                        "amount": {"amount": 25.45}
                    }
                ]
                """;
        Json json = Json.of(jsonString);

        // When
        List<PriceRule> result = fromJsonConverter.convert(json);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).priceListId().value()).isEqualTo(1);
        assertThat(result.get(1).priceListId().value()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON format")
    void shouldThrowExceptionForInvalidJson() {
        // Given
        Json invalidJson = Json.of("{ invalid json }");

        // When/Then
        assertThatThrownBy(() -> fromJsonConverter.convert(invalidJson))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to deserialize JSON to PriceRules");
    }

    @Test
    @DisplayName("Should throw exception for JSON with missing required fields")
    void shouldThrowExceptionForJsonWithMissingFields() {
        // Given - Missing 'amount' field
        String jsonString = """
                [
                    {
                        "priceListId": {"value": 1},
                        "startDate": "2020-06-14T00:00:00",
                        "endDate": "2020-12-31T23:59:59",
                        "priority": {"value": 0}
                    }
                ]
                """;
        Json json = Json.of(jsonString);

        // When/Then
        assertThatThrownBy(() -> fromJsonConverter.convert(json))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to deserialize JSON to PriceRules");
    }

    @Test
    @DisplayName("Should perform round-trip conversion correctly")
    void shouldPerformRoundTripConversionCorrectly() {
        // Given - Original list
        List<PriceRule> originalRules = List.of(
                new PriceRule(
                        new PriceListId(1),
                        LocalDateTime.of(2020, 6, 14, 0, 0),
                        LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                        new Priority(0),
                        new Money(new BigDecimal("35.50"))
                ),
                new PriceRule(
                        new PriceListId(2),
                        LocalDateTime.of(2020, 6, 14, 15, 0),
                        LocalDateTime.of(2020, 6, 14, 18, 30),
                        new Priority(1),
                        new Money(new BigDecimal("25.45"))
                )
        );

        // When - Convert to JSON and back
        Json json = toJsonConverter.convert(originalRules);
        List<PriceRule> reconstructedRules = fromJsonConverter.convert(json);

        // Then - Verify data integrity
        assertThat(reconstructedRules).hasSize(originalRules.size());

        for (int i = 0; i < originalRules.size(); i++) {
            PriceRule original = originalRules.get(i);
            PriceRule reconstructed = reconstructedRules.get(i);

            assertThat(reconstructed.priceListId()).isEqualTo(original.priceListId());
            assertThat(reconstructed.startDate()).isEqualTo(original.startDate());
            assertThat(reconstructed.endDate()).isEqualTo(original.endDate());
            assertThat(reconstructed.priority()).isEqualTo(original.priority());
            assertThat(reconstructed.amount().amount())
                    .isEqualByComparingTo(original.amount().amount());
        }
    }

    @Test
    @DisplayName("Should handle mutable list during conversion")
    void shouldHandleMutableListDuringConversion() {
        // Given - Mutable list
        List<PriceRule> mutableRules = new ArrayList<>();
        mutableRules.add(new PriceRule(
                new PriceListId(1),
                LocalDateTime.of(2020, 6, 14, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new Priority(0),
                new Money(new BigDecimal("35.50"))
        ));

        // When
        Json result = toJsonConverter.convert(mutableRules);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.asString()).contains("35.50");
    }
}

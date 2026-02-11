package com.inetum.prices.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inetum.prices.domain.model.PriceRule;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.List;

/**
 * R2DBC converters for serializing/deserializing List<PriceRule> to/from PostgreSQL JSONB.
 * <p>
 * These converters enable seamless mapping between the Java domain model and PostgreSQL's
 * JSONB column type in a reactive R2DBC context. They use Jackson ObjectMapper for JSON
 * serialization, matching the behavior of the original JPA converter.
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 *   <li>Uses Jackson (already in Spring Boot) for consistency with other converters</li>
 *   <li>JavaTimeModule registered for LocalDateTime serialization</li>
 *   <li>Fail-fast on deserialization errors to detect schema mismatches early</li>
 *   <li>Thread-safe static ObjectMapper instance shared across conversions</li>
 *   <li>Uses io.r2dbc.postgresql.codec.Json for PostgreSQL-specific JSONB support</li>
 * </ul>
 */
public class PriceRulesR2dbcConverter {

    private static final ObjectMapper MAPPER = createObjectMapper();
    private static final TypeReference<List<PriceRule>> TYPE_REFERENCE = new TypeReference<>() {};

    /**
     * Creates and configures the Jackson ObjectMapper.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register module for Java 8 time types (LocalDateTime)
        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps (use ISO-8601 strings instead)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    /**
     * Converter for writing List<PriceRule> to PostgreSQL JSONB.
     * <p>
     * Converts the domain model to JSON string and wraps it in R2DBC's Json type
     * for PostgreSQL JSONB column storage.
     */
    @WritingConverter
    public static class PriceRulesToJsonConverter implements Converter<List<PriceRule>, Json> {

        /**
         * Converts List<PriceRule> to PostgreSQL JSONB (wrapped in io.r2dbc.postgresql.codec.Json).
         *
         * @param rules the list of price rules to serialize
         * @return Json object containing the serialized rules
         * @throws IllegalStateException if serialization fails
         */
        @Override
        public Json convert(List<PriceRule> rules) {
            if (rules == null) {
                return Json.of("[]");
            }

            try {
                String json = MAPPER.writeValueAsString(rules);
                return Json.of(json);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Failed to serialize PriceRules to JSON: " + e.getMessage(), e
                );
            }
        }
    }

    /**
     * Converter for reading PostgreSQL JSONB to List<PriceRule>.
     * <p>
     * Unwraps the R2DBC Json type and deserializes the JSON string into the
     * domain model.
     */
    @ReadingConverter
    public static class JsonToPriceRulesConverter implements Converter<Json, List<PriceRule>> {

        /**
         * Converts PostgreSQL JSONB (io.r2dbc.postgresql.codec.Json) to List<PriceRule>.
         *
         * @param json the Json object from the database
         * @return deserialized list of price rules
         * @throws IllegalStateException if deserialization fails
         */
        @Override
        public List<PriceRule> convert(Json json) {
            if (json == null) {
                return List.of();
            }

            String jsonString = json.asString();
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return List.of();
            }

            try {
                return MAPPER.readValue(jsonString, TYPE_REFERENCE);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Failed to deserialize JSON to PriceRules: " + e.getMessage() +
                        "\nJSON content: " + jsonString, e
                );
            }
        }
    }
}

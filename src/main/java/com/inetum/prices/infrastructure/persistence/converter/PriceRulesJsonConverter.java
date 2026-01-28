package com.inetum.prices.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inetum.prices.domain.model.PriceRule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * JPA Attribute Converter for serializing/deserializing List<PriceRule> to/from JSONB.
 * <p>
 * This converter enables seamless mapping between the Java domain model and PostgreSQL's
 * JSONB column type. It uses Jackson ObjectMapper for JSON serialization.
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 *   <li>Uses Jackson (already in Spring Boot) instead of adding new dependencies</li>
 *   <li>JavaTimeModule registered for LocalDateTime serialization</li>
 *   <li>Fail-fast on unknown properties to detect schema mismatches early</li>
 *   <li>Thread-safe static ObjectMapper instance</li>
 * </ul>
 * <p>
 * <b>JSON Structure Example:</b>
 * <pre>
 * [
 *   {
 *     "priceListId": {"value": 1},
 *     "startDate": "2020-06-14T00:00:00",
 *     "endDate": "2020-12-31T23:59:59",
 *     "priority": {"value": 0},
 *     "amount": {"amount": 35.50}
 *   }
 * ]
 * </pre>
 */
@Converter
public class PriceRulesJsonConverter implements AttributeConverter<List<PriceRule>, String> {

    private static final ObjectMapper MAPPER = createObjectMapper();
    private static final TypeReference<List<PriceRule>> typeReference = new TypeReference<>() {};

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
     * Converts the List<PriceRule> domain object to a JSON string for database storage.
     *
     * @param rules the list of price rules to serialize
     * @return JSON string representation, or null if rules is null
     * @throws IllegalStateException if serialization fails
     */
    @Override
    public String convertToDatabaseColumn(List<PriceRule> rules) {
        if (rules == null) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize PriceRules to JSON: " + e.getMessage(), e
            );
        }
    }

    /**
     * Converts a JSON string from the database to a List<PriceRule> domain object.
     *
     * @param json the JSON string from the database
     * @return deserialized list of price rules, or null if json is null/empty
     * @throws IllegalStateException if deserialization fails
     */
    @Override
    public List<PriceRule> convertToEntityAttribute(String json) {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }

        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to deserialize JSON to PriceRules: " + e.getMessage() + 
                    "\nJSON content: " + json, e
            );
        }
    }
}

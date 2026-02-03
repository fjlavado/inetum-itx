package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Price Controller implementing the 5 mandatory test
 * scenarios.
 * <p>
 * These tests verify the complete end-to-end flow:
 * <ol>
 * <li>HTTP Request → REST Controller</li>
 * <li>Controller → Use Case (Domain Service)</li>
 * <li>Use Case → Repository Port</li>
 * <li>Repository Adapter → Spring Data JPA → PostgreSQL (Testcontainer)</li>
 * <li>Response back through all layers</li>
 * </ol>
 * <p>
 * <b>Test Data:</b> Loaded via Flyway migration V2__insert_test_data.sql
 * <p>
 * <b>Test Scenarios (as specified in requirements):</b>
 * <ul>
 * <li>Test 1: June 14 at 10:00 → Price List 1, 35.50 EUR</li>
 * <li>Test 2: June 14 at 16:00 → Price List 2, 25.45 EUR</li>
 * <li>Test 3: June 14 at 21:00 → Price List 1, 35.50 EUR</li>
 * <li>Test 4: June 15 at 10:00 → Price List 3, 30.50 EUR</li>
 * <li>Test 5: June 16 at 21:00 → Price List 4, 38.95 EUR</li>
 * </ul>
 */
@DisplayName("Price Controller Integration Tests - 5 Mandatory Scenarios")
class PriceControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L; // ZARA

    @ParameterizedTest(name = "Test {index}: Querying for {0} should return Price List {1} with {2} EUR")
    @CsvSource({
            "2020-06-14T10:00:00Z, 1, 35.50, 2020-06-14T00:00:00, 2020-12-31T23:59:59",
            "2020-06-14T16:00:00Z, 2, 25.45, 2020-06-14T15:00:00, 2020-06-14T18:30:00",
            "2020-06-14T21:00:00Z, 1, 35.50, 2020-06-14T00:00:00, 2020-12-31T23:59:59",
            "2020-06-15T10:00:00Z, 3, 30.50, 2020-06-15T00:00:00, 2020-06-15T11:00:00",
            "2020-06-16T21:00:00Z, 4, 38.95, 2020-06-15T16:00:00, 2020-12-31T23:59:59",
            "2020-06-14T00:00:00Z, 1, 35.50, 2020-06-14T00:00:00, 2020-12-31T23:59:59",
            "2020-06-14T15:00:00Z, 2, 25.45, 2020-06-14T15:00:00, 2020-06-14T18:30:00",
            "2020-06-14T18:30:00Z, 2, 25.45, 2020-06-14T15:00:00, 2020-06-14T18:30:00",
            "2020-12-31T23:59:59Z, 4, 38.95, 2020-06-15T16:00:00, 2020-12-31T23:59:59"
    })
    @DisplayName("Implementing the 5 mandatory test scenarios")
    void shouldReturnCorrectPriceForMandatoryScenarios(
            String applicationDate, int expectedPriceList, String expectedPrice,
            String expectedStartDate, String expectedEndDate) {

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(expectedPriceList);
        assertThat(priceResponse.getPrice()).isEqualByComparingTo(new BigDecimal(expectedPrice));
        assertThat(priceResponse.getStartDate().toLocalDateTime()).isEqualTo(LocalDateTime.parse(expectedStartDate));
        assertThat(priceResponse.getEndDate().toLocalDateTime()).isEqualTo(LocalDateTime.parse(expectedEndDate));
    }

    @Test
    @DisplayName("Should return 404 when no price exists for product/brand/date")
    void shouldReturn404WhenNoPriceFound() {
        // Given - non-existent product
        String url = String.format(
                "/prices?applicationDate=%s&productId=%d&brandId=%d",
                "2020-06-14T10:00:00Z",
                99999L,
                BRAND_ID);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("No applicable price found");

        System.out.println("✅ 404 Test PASSED");
    }

    @Test
    @DisplayName("Should return 400 when request parameters are invalid")
    void shouldReturn400WhenParametersAreInvalid() {
        // Given - invalid product ID
        String url = String.format(
                "/prices?applicationDate=%s&productId=%s&brandId=%d",
                "2020-06-14T10:00:00Z",
                "invalid",
                BRAND_ID);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        System.out.println("✅ 400 Test PASSED");
    }

    /**
     * Helper method to query the price endpoint.
     *
     * @param applicationDate ISO-8601 formatted date string
     * @return ResponseEntity with PriceResponse
     */
    private ResponseEntity<PriceResponse> queryPrice(String applicationDate) {
        String url = String.format(
                "/prices?applicationDate=%s&productId=%d&brandId=%d",
                applicationDate,
                PRODUCT_ID,
                BRAND_ID);
        return restTemplate.getForEntity(url, PriceResponse.class);
    }
}

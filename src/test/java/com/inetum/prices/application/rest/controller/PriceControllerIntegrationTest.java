package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Price Controller implementing the 5 mandatory test scenarios.
 * <p>
 * These tests verify the complete end-to-end flow:
 * <ol>
 *   <li>HTTP Request → REST Controller</li>
 *   <li>Controller → Use Case (Domain Service)</li>
 *   <li>Use Case → Repository Port</li>
 *   <li>Repository Adapter → Spring Data JPA → PostgreSQL (Testcontainer)</li>
 *   <li>Response back through all layers</li>
 * </ol>
 * <p>
 * <b>Test Data:</b> Loaded via Flyway migration V2__insert_test_data.sql
 * <p>
 * <b>Test Scenarios (as specified in requirements):</b>
 * <ul>
 *   <li>Test 1: June 14 at 10:00 → Price List 1, 35.50 EUR</li>
 *   <li>Test 2: June 14 at 16:00 → Price List 2, 25.45 EUR</li>
 *   <li>Test 3: June 14 at 21:00 → Price List 1, 35.50 EUR</li>
 *   <li>Test 4: June 15 at 10:00 → Price List 3, 30.50 EUR</li>
 *   <li>Test 5: June 16 at 21:00 → Price List 4, 38.95 EUR</li>
 * </ul>
 */
@DisplayName("Price Controller Integration Tests - 5 Mandatory Scenarios")
class PriceControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L; // ZARA

    @Test
    @DisplayName("Test 1: Day 14 at 10:00 - Should return Price List 1 with 35.50 EUR")
    void test1_day14At10_shouldReturnPriceList1() {
        // Given
        String applicationDate = "2020-06-14T10:00:00";

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.productId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.brandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.priceList()).isEqualTo(1);
        assertThat(priceResponse.price()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(priceResponse.startDate()).isEqualTo(LocalDateTime.parse("2020-06-14T00:00:00"));
        assertThat(priceResponse.endDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));
        
        System.out.println("✅ Test 1 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 2: Day 14 at 16:00 - Should return Price List 2 with 25.45 EUR (higher priority)")
    void test2_day14At16_shouldReturnPriceList2() {
        // Given
        String applicationDate = "2020-06-14T16:00:00";

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.productId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.brandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.priceList()).isEqualTo(2);
        assertThat(priceResponse.price()).isEqualByComparingTo(new BigDecimal("25.45"));
        assertThat(priceResponse.startDate()).isEqualTo(LocalDateTime.parse("2020-06-14T15:00:00"));
        assertThat(priceResponse.endDate()).isEqualTo(LocalDateTime.parse("2020-06-14T18:30:00"));
        
        System.out.println("✅ Test 2 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 3: Day 14 at 21:00 - Should return Price List 1 with 35.50 EUR (promotion ended)")
    void test3_day14At21_shouldReturnPriceList1() {
        // Given
        String applicationDate = "2020-06-14T21:00:00";

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.productId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.brandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.priceList()).isEqualTo(1);
        assertThat(priceResponse.price()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertThat(priceResponse.startDate()).isEqualTo(LocalDateTime.parse("2020-06-14T00:00:00"));
        assertThat(priceResponse.endDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));
        
        System.out.println("✅ Test 3 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 4: Day 15 at 10:00 - Should return Price List 3 with 30.50 EUR")
    void test4_day15At10_shouldReturnPriceList3() {
        // Given
        String applicationDate = "2020-06-15T10:00:00";

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.productId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.brandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.priceList()).isEqualTo(3);
        assertThat(priceResponse.price()).isEqualByComparingTo(new BigDecimal("30.50"));
        assertThat(priceResponse.startDate()).isEqualTo(LocalDateTime.parse("2020-06-15T00:00:00"));
        assertThat(priceResponse.endDate()).isEqualTo(LocalDateTime.parse("2020-06-15T11:00:00"));
        
        System.out.println("✅ Test 4 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 5: Day 16 at 21:00 - Should return Price List 4 with 38.95 EUR")
    void test5_day16At21_shouldReturnPriceList4() {
        // Given
        String applicationDate = "2020-06-16T21:00:00";

        // When
        ResponseEntity<PriceResponse> response = queryPrice(applicationDate);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        PriceResponse priceResponse = response.getBody();
        assertThat(priceResponse.productId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.brandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.priceList()).isEqualTo(4);
        assertThat(priceResponse.price()).isEqualByComparingTo(new BigDecimal("38.95"));
        assertThat(priceResponse.startDate()).isEqualTo(LocalDateTime.parse("2020-06-15T16:00:00"));
        assertThat(priceResponse.endDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));
        
        System.out.println("✅ Test 5 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Should return 404 when no price exists for product/brand/date")
    void shouldReturn404WhenNoPriceFound() {
        // Given - non-existent product
        String url = String.format(
                "/prices?applicationDate=%s&productId=%d&brandId=%d",
                "2020-06-14T10:00:00",
                99999L,
                BRAND_ID
        );

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
                "2020-06-14T10:00:00",
                "invalid",
                BRAND_ID
        );

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
                BRAND_ID
        );
        return restTemplate.getForEntity(url, PriceResponse.class);
    }
}

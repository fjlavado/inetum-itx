package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.dto.generated.PriceResponse;
import com.inetum.prices.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reactive integration tests for the OpenAPI-based Price API implementing the 5 mandatory test scenarios.
 * <p>
 * These tests verify the complete end-to-end reactive flow:
 * <ol>
 *   <li>HTTP Request → PriceController (OpenAPI-generated interface implementation)</li>
 *   <li>Controller → Use Case (Reactive Domain Service)</li>
 *   <li>Use Case → Repository Port</li>
 *   <li>Repository Adapter → Spring Data R2DBC → PostgreSQL (Testcontainer)</li>
 *   <li>Response back through all layers (reactive Mono chain)</li>
 * </ol>
 * <p>
 * <b>API Contract:</b> Defined in openapi.yml, enforced via OpenAPI Generator
 * <br>
 * <b>Endpoint:</b> GET /v1/prices (versioned API)
 * <br>
 * <b>Response DTOs:</b> OpenAPI-generated PriceResponse and ErrorResponse
 * <p>
 * <b>Test Data:</b> Loaded via Flyway migration V2__insert_test_data.sql and V4__migrate_to_timelines.sql
 * <p>
 * <b>Test Scenarios (as specified in requirements):</b>
 * <ul>
 *   <li>Test 1: June 14 at 10:00 → Price List 1, 35.50 EUR</li>
 *   <li>Test 2: June 14 at 16:00 → Price List 2, 25.45 EUR (higher priority)</li>
 *   <li>Test 3: June 14 at 21:00 → Price List 1, 35.50 EUR (promotion ended)</li>
 *   <li>Test 4: June 15 at 10:00 → Price List 3, 30.50 EUR</li>
 *   <li>Test 5: June 16 at 21:00 → Price List 4, 38.95 EUR</li>
 * </ul>
 * <p>
 * <b>Testing Approach:</b>
 * Uses Spring WebFlux's WebTestClient for testing reactive endpoints.
 * WebTestClient provides a fluent API for testing WebFlux applications with
 * built-in support for reactive types (Mono, Flux).
 */
@DisplayName("Reactive Price API Integration Tests - 5 Mandatory Scenarios")
class PriceControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final Long PRODUCT_ID = 35455L;
    private static final Long BRAND_ID = 1L; // ZARA

    @Test
    @DisplayName("Test 1: Day 14 at 10:00 - Should return Price List 1 with 35.50 EUR")
    void test1_day14At10_shouldReturnPriceList1() {
        // Given
        String applicationDate = "2020-06-14T10:00:00";

        // When & Then
        PriceResponse priceResponse = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/prices")
                        .queryParam("applicationDate", applicationDate)
                        .queryParam("productId", PRODUCT_ID)
                        .queryParam("brandId", BRAND_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PriceResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        assertThat(priceResponse).isNotNull();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(1);
        assertThat(priceResponse.getPrice()).isEqualTo(35.50);
        assertThat(priceResponse.getStartDate()).isEqualTo(LocalDateTime.parse("2020-06-14T00:00:00"));
        assertThat(priceResponse.getEndDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));

        System.out.println("✅ Test 1 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 2: Day 14 at 16:00 - Should return Price List 2 with 25.45 EUR (higher priority)")
    void test2_day14At16_shouldReturnPriceList2() {
        // Given
        String applicationDate = "2020-06-14T16:00:00";

        // When & Then
        PriceResponse priceResponse = queryPrice(applicationDate);

        assertThat(priceResponse).isNotNull();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(2);
        assertThat(priceResponse.getPrice()).isEqualTo(25.45);
        assertThat(priceResponse.getStartDate()).isEqualTo(LocalDateTime.parse("2020-06-14T15:00:00"));
        assertThat(priceResponse.getEndDate()).isEqualTo(LocalDateTime.parse("2020-06-14T18:30:00"));

        System.out.println("✅ Test 2 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 3: Day 14 at 21:00 - Should return Price List 1 with 35.50 EUR (promotion ended)")
    void test3_day14At21_shouldReturnPriceList1() {
        // Given
        String applicationDate = "2020-06-14T21:00:00";

        // When & Then
        PriceResponse priceResponse = queryPrice(applicationDate);

        assertThat(priceResponse).isNotNull();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(1);
        assertThat(priceResponse.getPrice()).isEqualTo(35.50);
        assertThat(priceResponse.getStartDate()).isEqualTo(LocalDateTime.parse("2020-06-14T00:00:00"));
        assertThat(priceResponse.getEndDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));

        System.out.println("✅ Test 3 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 4: Day 15 at 10:00 - Should return Price List 3 with 30.50 EUR")
    void test4_day15At10_shouldReturnPriceList3() {
        // Given
        String applicationDate = "2020-06-15T10:00:00";

        // When & Then
        PriceResponse priceResponse = queryPrice(applicationDate);

        assertThat(priceResponse).isNotNull();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(3);
        assertThat(priceResponse.getPrice()).isEqualTo(30.50);
        assertThat(priceResponse.getStartDate()).isEqualTo(LocalDateTime.parse("2020-06-15T00:00:00"));
        assertThat(priceResponse.getEndDate()).isEqualTo(LocalDateTime.parse("2020-06-15T11:00:00"));

        System.out.println("✅ Test 4 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Test 5: Day 16 at 21:00 - Should return Price List 4 with 38.95 EUR")
    void test5_day16At21_shouldReturnPriceList4() {
        // Given
        String applicationDate = "2020-06-16T21:00:00";

        // When & Then
        PriceResponse priceResponse = queryPrice(applicationDate);

        assertThat(priceResponse).isNotNull();
        assertThat(priceResponse.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(priceResponse.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(priceResponse.getPriceList()).isEqualTo(4);
        assertThat(priceResponse.getPrice()).isEqualTo(38.95);
        assertThat(priceResponse.getStartDate()).isEqualTo(LocalDateTime.parse("2020-06-15T16:00:00"));
        assertThat(priceResponse.getEndDate()).isEqualTo(LocalDateTime.parse("2020-12-31T23:59:59"));

        System.out.println("✅ Test 5 PASSED: " + priceResponse);
    }

    @Test
    @DisplayName("Should return 404 when no price exists for product/brand/date")
    void shouldReturn404WhenNoPriceFound() {
        // Given - non-existent product
        Long nonExistentProductId = 99999L;

        // When & Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/prices")
                        .queryParam("applicationDate", "2020-06-14T10:00:00")
                        .queryParam("productId", nonExistentProductId)
                        .queryParam("brandId", BRAND_ID)
                        .build())
                .exchange()
                .expectStatus().isNotFound();

        System.out.println("✅ 404 Test PASSED");
    }

    @Test
    @DisplayName("Should return 400 when request parameters are invalid")
    void shouldReturn400WhenParametersAreInvalid() {
        // Given - invalid product ID (string instead of number)
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/prices")
                        .queryParam("applicationDate", "2020-06-14T10:00:00")
                        .queryParam("productId", "invalid")
                        .queryParam("brandId", BRAND_ID)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();

        System.out.println("✅ 400 Test PASSED");
    }

    /**
     * Helper method to query the price endpoint reactively.
     *
     * @param applicationDate ISO-8601 formatted date string
     * @return PriceResponse from the API
     */
    private PriceResponse queryPrice(String applicationDate) {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/prices")
                        .queryParam("applicationDate", applicationDate)
                        .queryParam("productId", PRODUCT_ID)
                        .queryParam("brandId", BRAND_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PriceResponse.class)
                .returnResult()
                .getResponseBody();
    }
}

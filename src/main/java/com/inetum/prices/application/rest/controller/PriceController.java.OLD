package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST Controller for the Prices API.
 * <p>
 * This controller exposes the pricing query endpoint as specified in the requirements.
 * It acts as a driving adapter in the hexagonal architecture, translating HTTP requests
 * into use case invocations and domain responses into HTTP responses.
 * <p>
 * <b>API Endpoint:</b>
 * <ul>
 *   <li>GET /prices?applicationDate={date}&productId={id}&brandId={id}</li>
 * </ul>
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Handle HTTP request/response concerns</li>
 *   <li>Validate and parse query parameters</li>
 *   <li>Convert primitives to value objects</li>
 *   <li>Invoke domain use case</li>
 *   <li>Map domain model to REST DTO</li>
 * </ul>
 */
@RestController
@RequestMapping("/prices")
public class PriceController {

    private final GetPriceUseCase getPriceUseCase;

    /**
     * Constructor injection of the use case.
     * <p>
     * Spring will autowire the implementation configured in PriceConfiguration.
     *
     * @param getPriceUseCase the price query use case
     */
    public PriceController(GetPriceUseCase getPriceUseCase) {
        this.getPriceUseCase = getPriceUseCase;
    }

    /**
     * GET /prices endpoint - retrieves the applicable price for a product/brand at a specific date.
     * <p>
     * <b>Example Request:</b>
     * <pre>
     * GET /prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1
     * </pre>
     * <p>
     * <b>Example Response (200 OK):</b>
     * <pre>
     * {
     *   "productId": 35455,
     *   "brandId": 1,
     *   "priceList": 1,
     *   "startDate": "2020-06-14T00:00:00",
     *   "endDate": "2020-12-31T23:59:59",
     *   "price": 35.50
     * }
     * </pre>
     *
     * @param applicationDate the date/time to query (ISO-8601 format)
     * @param productId       the product identifier
     * @param brandId         the brand identifier
     * @return ResponseEntity with PriceResponse (200) or error (404, 400)
     */
    @GetMapping
    public ResponseEntity<PriceResponse> getPrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NotNull LocalDateTime applicationDate,
            @NotNull @RequestParam Long productId,
            @NotNull @RequestParam Long brandId
    ) {
        // Convert primitives to value objects
        ProductId productValueObject = new ProductId(productId);
        BrandId brandValueObject = new BrandId(brandId);

        // Invoke use case
        Price price = getPriceUseCase.getApplicablePrice(
                applicationDate,
                productValueObject,
                brandValueObject
        );

        // Map domain model to DTO
        PriceResponse response = mapToResponse(price);

        // Return HTTP 200 OK with body
        return ResponseEntity.ok(response);
    }

    /**
     * Maps the domain Price aggregate to a REST API PriceResponse DTO.
     *
     * @param price the domain price
     * @return the REST DTO
     */
    private PriceResponse mapToResponse(Price price) {
        return new PriceResponse(
                price.productId().value(),
                price.brandId().value(),
                price.priceListId().value(),
                price.startDate(),
                price.endDate(),
                price.amount().amount()
        );
    }
}

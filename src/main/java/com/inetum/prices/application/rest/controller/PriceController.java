package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.api.PricesApi;
import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.application.rest.mapper.PriceMapper;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * REST Controller for the Prices API.
 * <p>
 * This controller exposes the pricing query endpoint as specified in the
 * requirements.
 * It acts as a driving adapter in the hexagonal architecture, translating HTTP
 * requests
 * into use case invocations and domain responses into HTTP responses.
 * <p>
 * <b>API Endpoint:</b>
 * <ul>
 * <li>GET /prices?applicationDate={date}&productId={id}&brandId={id}</li>
 * </ul>
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Handle HTTP request/response concerns</li>
 * <li>Validate and parse query parameters</li>
 * <li>Convert primitives to value objects</li>
 * <li>Invoke domain use case</li>
 * <li>Map domain model to REST DTO</li>
 * </ul>
 */
@RestController
public class PriceController implements PricesApi {

    private final GetPriceUseCase getPriceUseCase;
    private final PriceMapper priceMapper;

    /**
     * Constructor injection of the use case and mapper.
     *
     * @param getPriceUseCase the price query use case
     * @param priceMapper     the MapStruct mapper for API DTOs
     */
    public PriceController(GetPriceUseCase getPriceUseCase, PriceMapper priceMapper) {
        this.getPriceUseCase = getPriceUseCase;
        this.priceMapper = priceMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PriceResponse> getPrice(
            OffsetDateTime applicationDate,
            Long productId,
            Long brandId) {
        // Convert OffsetDateTime to LocalDateTime for domain (assuming simple local
        // conversion)
        LocalDateTime domainDate = applicationDate.toLocalDateTime();

        // Convert primitives to value objects
        ProductId productValueObject = new ProductId(productId);
        BrandId brandValueObject = new BrandId(brandId);

        // Invoke use case
        Price price = getPriceUseCase.getApplicablePrice(
                domainDate,
                productValueObject,
                brandValueObject);

        // Map domain model to DTO using MapStruct
        PriceResponse response = priceMapper.toResponse(price);

        // Return HTTP 200 OK with body
        return ResponseEntity.ok(response);
    }
}

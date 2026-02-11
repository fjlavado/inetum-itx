package com.inetum.prices.application.rest.controller;

import com.inetum.prices.application.rest.api.generated.V1Api;
import com.inetum.prices.application.rest.dto.generated.PriceResponse;
import com.inetum.prices.application.rest.mapper.PriceApiMapper;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * REST Controller implementing the OpenAPI-generated Prices API interface.
 * <p>
 * This controller serves as the DRIVING ADAPTER in hexagonal architecture,
 * translating HTTP requests into domain operations via the GetPriceUseCase port.
 * <p>
 * <b>Architecture:</b>
 * <pre>
 * HTTP Request
 *   ↓
 * PriceController (this class)
 *   ↓ (primitives → value objects via PriceApiMapper)
 * GetPriceUseCase (domain port interface)
 *   ↓
 * PriceService (domain logic implementation)
 *   ↓ (domain Price returned)
 * PriceController (domain Price → PriceResponse DTO via PriceApiMapper)
 *   ↓
 * HTTP Response
 * </pre>
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 *   <li>Implements generated {@link V1Api} interface for compile-time contract enforcement</li>
 *   <li>Uses {@link PriceApiMapper} for clean domain ↔ DTO conversion</li>
 *   <li>Returns {@code Mono<ResponseEntity<T>>} for reactive, non-blocking execution</li>
 *   <li>Uses {@code Mono.defer()} for proper lazy evaluation</li>
 *   <li>Delegates all business logic to domain layer (zero logic in controller)</li>
 *   <li>Error handling delegated to {@link com.inetum.prices.application.rest.exception.GlobalExceptionHandler}</li>
 * </ul>
 * <p>
 * <b>Reactive Flow:</b>
 * <ol>
 *   <li>Convert primitives (Long) to value objects (ProductId, BrandId)</li>
 *   <li>Invoke use case with domain types → returns {@code Mono<Price>}</li>
 *   <li>Map domain Price to PriceResponse DTO</li>
 *   <li>Wrap in ResponseEntity with 200 OK status</li>
 *   <li>Errors propagate as Mono.error() and are handled by GlobalExceptionHandler</li>
 * </ol>
 *
 * @see V1Api Generated OpenAPI interface defining the contract
 * @see GetPriceUseCase Domain use case port
 * @see PriceApiMapper Mapper for domain ↔ DTO conversion
 */
@RestController
@Slf4j
public class PriceController implements V1Api {

    private final GetPriceUseCase getPriceUseCase;
    private final PriceApiMapper mapper;

    /**
     * Constructor-based dependency injection.
     *
     * @param getPriceUseCase the domain use case for querying prices
     * @param mapper          the mapper for domain ↔ DTO conversion
     */
    public PriceController(GetPriceUseCase getPriceUseCase, PriceApiMapper mapper) {
        this.getPriceUseCase = getPriceUseCase;
        this.mapper = mapper;
    }

    /**
     * Retrieves the applicable price for a product and brand at a specific date/time.
     * <p>
     * This method implements the OpenAPI contract defined in openapi.yml.
     * <p>
     * <b>Parameter Validation:</b> Handled by generated interface annotations (@Valid, @NotNull, @Min)
     * <br>
     * <b>Error Handling:</b> Delegated to GlobalExceptionHandler
     * <br>
     * <b>Reactive Execution:</b> Uses Mono.defer() for lazy evaluation and proper reactive chain composition
     *
     * @param applicationDate the date/time to evaluate price applicability (validated: not null, ISO-8601)
     * @param productId       the product identifier (validated: not null, >= 1)
     * @param brandId         the brand identifier (validated: not null, >= 1)
     * @param exchange        the ServerWebExchange (provided by Spring WebFlux, hidden from OpenAPI)
     * @return Mono emitting ResponseEntity with PriceResponse (200 OK) or error signal (404, 400, 500)
     */
    @Override
    public Mono<ResponseEntity<PriceResponse>> getPrice(
            LocalDateTime applicationDate,
            Long productId,
            Long brandId,
            ServerWebExchange exchange) {

        log.debug("Received price query: applicationDate={}, productId={}, brandId={}",
                applicationDate, productId, brandId);

        return Mono.defer(() -> {
            // Convert primitives to domain value objects using mapper
            ProductId productIdVo = mapper.toProductId(productId);
            BrandId brandIdVo = mapper.toBrandId(brandId);

            // Invoke use case reactively (returns Mono<Price>)
            return getPriceUseCase.getApplicablePrice(applicationDate, productIdVo, brandIdVo)
                    // Map domain Price to PriceResponse DTO
                    .map(mapper::toPriceResponse)
                    // Log success
                    .doOnNext(response -> log.debug("Price found: {}", response))
                    // Wrap in ResponseEntity with 200 OK
                    .map(ResponseEntity::ok);
            // Note: Errors propagate as Mono.error() and are handled by GlobalExceptionHandler
        });
    }
}

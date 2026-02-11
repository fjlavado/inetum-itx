package com.inetum.prices.application.rest.handler;

import com.inetum.prices.application.rest.dto.PriceResponse;
import com.inetum.prices.domain.exception.PriceNotFoundException;
import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.inbound.GetPriceUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Reactive handler for price queries using Spring WebFlux functional endpoints.
 * <p>
 * This handler implements the reactive version of the pricing API, replacing the
 * traditional @RestController approach with functional programming style. It leverages
 * Project Reactor for non-blocking, asynchronous request handling.
 * <p>
 * <b>Handler Responsibilities:</b>
 * <ul>
 *   <li>Extract and validate query parameters from ServerRequest</li>
 *   <li>Convert primitives to domain value objects</li>
 *   <li>Invoke domain use case reactively</li>
 *   <li>Map domain model to REST DTO</li>
 *   <li>Handle errors reactively with appropriate HTTP status codes</li>
 * </ul>
 * <p>
 * <b>Error Handling Strategy:</b>
 * <ul>
 *   <li>Missing/invalid parameters → 400 Bad Request</li>
 *   <li>PriceNotFoundException → 404 Not Found</li>
 *   <li>Other exceptions → 500 Internal Server Error</li>
 * </ul>
 * <p>
 * <b>Performance Characteristics:</b>
 * Fully non-blocking execution from HTTP request to database query and back.
 * Leverages reactive backpressure to handle high concurrency efficiently.
 */
@Component
public class PriceHandler {

    private final GetPriceUseCase getPriceUseCase;

    /**
     * Constructor injection of the use case.
     *
     * @param getPriceUseCase the reactive price query use case
     */
    public PriceHandler(GetPriceUseCase getPriceUseCase) {
        this.getPriceUseCase = getPriceUseCase;
    }

    /**
     * Handles GET requests for price queries reactively.
     * <p>
     * <b>Expected Query Parameters:</b>
     * <ul>
     *   <li>applicationDate: ISO-8601 date-time string (e.g., "2020-06-14T10:00:00")</li>
     *   <li>productId: Long product identifier</li>
     *   <li>brandId: Long brand identifier</li>
     * </ul>
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
     * @param request the incoming server request
     * @return Mono emitting ServerResponse with PriceResponse body or error
     */
    public Mono<ServerResponse> getPrice(ServerRequest request) {
        return extractAndValidateParameters(request)
                .flatMap(params -> {
                    // Convert primitives to value objects
                    ProductId productId = new ProductId(params.productId);
                    BrandId brandId = new BrandId(params.brandId);

                    // Invoke use case reactively
                    return getPriceUseCase.getApplicablePrice(
                            params.applicationDate,
                            productId,
                            brandId
                    );
                })
                // Map domain model to DTO
                .map(this::mapToResponse)
                // Build successful response
                .flatMap(priceResponse ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(priceResponse)
                )
                // Handle errors reactively
                .onErrorResume(this::handleError);
    }

    /**
     * Extracts and validates query parameters from the request.
     * <p>
     * This method validates that all required parameters are present and
     * can be parsed to their expected types. It returns a Mono that either
     * emits valid parameters or errors with IllegalArgumentException.
     *
     * @param request the server request
     * @return Mono emitting validated parameters
     */
    private Mono<QueryParams> extractAndValidateParameters(ServerRequest request) {
        return Mono.fromCallable(() -> {
            // Extract applicationDate
            String dateStr = request.queryParam("applicationDate")
                    .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: applicationDate"));

            LocalDateTime applicationDate;
            try {
                applicationDate = LocalDateTime.parse(dateStr);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Invalid applicationDate format. Expected ISO-8601 format (e.g., '2020-06-14T10:00:00'): " + e.getMessage()
                );
            }

            // Extract productId
            Long productId = request.queryParam("productId")
                    .map(s -> {
                        try {
                            return Long.parseLong(s);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid productId: must be a number");
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: productId"));

            // Extract brandId
            Long brandId = request.queryParam("brandId")
                    .map(s -> {
                        try {
                            return Long.parseLong(s);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid brandId: must be a number");
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Missing required parameter: brandId"));

            return new QueryParams(applicationDate, productId, brandId);
        });
    }

    /**
     * Maps the domain Price to a REST API PriceResponse DTO.
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

    /**
     * Handles errors reactively by mapping exceptions to appropriate HTTP responses.
     * <p>
     * <b>Error Mapping:</b>
     * <ul>
     *   <li>IllegalArgumentException → 400 Bad Request</li>
     *   <li>PriceNotFoundException → 404 Not Found</li>
     *   <li>Other exceptions → 500 Internal Server Error</li>
     * </ul>
     *
     * @param throwable the error to handle
     * @return Mono emitting an error ServerResponse
     */
    private Mono<ServerResponse> handleError(Throwable throwable) {
        if (throwable instanceof IllegalArgumentException) {
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse(throwable.getMessage()));
        } else if (throwable instanceof PriceNotFoundException) {
            return ServerResponse.notFound().build();
        } else {
            return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse("Internal server error: " + throwable.getMessage()));
        }
    }

    /**
     * Internal record to hold validated query parameters.
     */
    private record QueryParams(LocalDateTime applicationDate, Long productId, Long brandId) {}

    /**
     * Internal record for error response body.
     */
    private record ErrorResponse(String error) {}
}

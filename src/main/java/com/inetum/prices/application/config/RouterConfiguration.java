package com.inetum.prices.application.config;

import com.inetum.prices.application.rest.handler.PriceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for reactive functional endpoints.
 * <p>
 * This configuration defines the routing rules for the Prices API using Spring WebFlux's
 * functional programming model. It replaces traditional @RestController annotations with
 * RouterFunction beans that map HTTP requests to handler methods.
 * <p>
 * <b>Benefits of Functional Endpoints:</b>
 * <ul>
 *   <li>More explicit and composable routing logic</li>
 *   <li>Better testability (routes can be tested without full context)</li>
 *   <li>Functional programming style aligns with reactive paradigm</li>
 *   <li>Less annotation magic, more explicit configuration</li>
 * </ul>
 * <p>
 * <b>API Routes:</b>
 * <ul>
 *   <li>GET /prices → PriceHandler.getPrice()</li>
 * </ul>
 */
@Configuration
public class RouterConfiguration {

    /**
     * Defines the routing rules for the Prices API.
     * <p>
     * This RouterFunction maps HTTP GET requests to /prices to the appropriate
     * handler method. It uses predicate composition for request matching.
     * <p>
     * <b>Route Definition:</b>
     * <pre>
     * GET /prices?applicationDate={date}&productId={id}&brandId={id}
     *   → PriceHandler.getPrice()
     *   → Returns: 200 OK with PriceResponse JSON
     *            or 404 Not Found
     *            or 400 Bad Request
     * </pre>
     *
     * @param priceHandler the handler for price queries
     * @return RouterFunction defining the API routes
     */
    @Bean
    public RouterFunction<ServerResponse> priceRoutes(PriceHandler priceHandler) {
        return RouterFunctions.route()
                .GET("/prices", accept(MediaType.APPLICATION_JSON), priceHandler::getPrice)
                .build();
    }
}

package com.inetum.prices.infrastructure.persistence.adapter;

import com.inetum.prices.domain.model.Price;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;
import com.inetum.prices.domain.ports.outbound.PriceRepositoryPort;
import com.inetum.prices.infrastructure.persistence.entity.PriceEntity;
import com.inetum.prices.infrastructure.persistence.mapper.PriceEntityMapper;
import com.inetum.prices.infrastructure.persistence.repository.SpringDataPriceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PostgreSQL implementation of the PriceRepositoryPort.
 * <p>
 * This adapter bridges the domain layer (PriceRepositoryPort) with the infrastructure
 * layer (Spring Data JPA repository). It implements the hexagonal architecture pattern
 * by adapting infrastructure concerns to domain needs.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Delegate database queries to Spring Data repository</li>
 *   <li>Map infrastructure entities to domain models using MapStruct</li>
 *   <li>Handle any infrastructure exceptions (if needed)</li>
 *   <li>Ensure domain layer remains independent of JPA/PostgreSQL</li>
 * </ul>
 * <p>
 * <b>Design Note:</b> This class uses Spring's @Component annotation, making it
 * discoverable by component scanning. It will be autowired into the domain service
 * via the Spring configuration class.
 */
@Component
public class PostgreSQLPriceRepositoryAdapter implements PriceRepositoryPort {

    private final SpringDataPriceRepository springDataRepository;
    private final PriceEntityMapper mapper;

    /**
     * Constructs the adapter with required dependencies.
     * <p>
     * Spring will automatically inject both dependencies via constructor injection.
     *
     * @param springDataRepository the Spring Data JPA repository
     * @param mapper               the MapStruct mapper
     */
    public PostgreSQLPriceRepositoryAdapter(
            SpringDataPriceRepository springDataRepository,
            PriceEntityMapper mapper
    ) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation:</b>
     * <ol>
     *   <li>Extract primitive values from value objects</li>
     *   <li>Query Spring Data repository</li>
     *   <li>Map entities to domain models using MapStruct</li>
     *   <li>Return list of domain Price aggregates</li>
     * </ol>
     */
    @Override
    public List<Price> findApplicablePrices(
            LocalDateTime applicationDate,
            ProductId productId,
            BrandId brandId
    ) {
        // Input validation
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (brandId == null) {
            throw new IllegalArgumentException("BrandId cannot be null");
        }

        // Extract primitive values from value objects
        Long brandIdValue = brandId.value();
        Long productIdValue = productId.value();

        // Query database using Spring Data repository
        List<PriceEntity> entities = springDataRepository.findApplicablePrices(
                brandIdValue,
                productIdValue,
                applicationDate
        );

        // Map entities to domain models and return
        return mapper.toDomainList(entities);
    }
}

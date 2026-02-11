package com.inetum.prices.domain.model;

import com.inetum.prices.domain.exception.DomainErrorException;
import com.inetum.prices.domain.model.valueobject.BrandId;
import com.inetum.prices.domain.model.valueobject.ProductId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.inetum.prices.domain.exception.DomainErrorException.requireNonNull;
import static com.inetum.prices.domain.exception.DomainErrorException.require;

/**
 * Aggregate Root: All pricing rules for a single product+brand combination.
 * <p>
 * This is the core of the CQRS refactoring. Instead of storing each price as
 * a separate row (old approach), we aggregate ALL pricing rules for a product
 * into a single entity stored as JSONB in PostgreSQL.
 * <p>
 * <b>CQRS Pattern Benefits:</b>
 * <ul>
 *   <li>O(1) database lookup by product+brand (primary key)</li>
 *   <li>In-memory filtering replaces SQL BETWEEN queries</li>
 *   <li>Easy to cache (one key per product)</li>
 *   <li>Reduced database round trips</li>
 *   <li>JSONB enables flexible schema evolution</li>
 * </ul>
 * <p>
 * <b>Business Logic:</b>
 * The {@code getEffectivePrice} method encapsulates the core business rule:
 * when multiple pricing rules overlap for a given timestamp, the rule with
 * the highest priority wins.
 * <p>
 * <b>Invariants:</b>
 * <ul>
 *   <li>Must have at least one pricing rule</li>
 *   <li>All rules must be valid (enforced by PriceRule constructor)</li>
 *   <li>ProductId and BrandId are immutable</li>
 * </ul>
 */
public class ProductPriceTimeline {

    private final ProductId productId;
    private final BrandId brandId;
    private final List<PriceRule> rules;

    /**
     * Constructs a new ProductPriceTimeline.
     *
     * @param productId the product identifier
     * @param brandId   the brand identifier
     * @param rules     list of pricing rules (must not be empty)
     * @throws IllegalArgumentException if any parameter is null or rules is empty
     */
    public ProductPriceTimeline(ProductId productId, BrandId brandId, List<PriceRule> rules) {

        requireNonNull(productId, "ProductId cannot be null")
                .or(() -> requireNonNull(brandId, "BrandId cannot be null"))
                .or(() -> requireNonNull(rules, "rules cannot be null"))
                .or(() -> require(() -> !rules.isEmpty(), "Rules cannot be empty - must have at least one pricing rule"))
                .ifPresent(ex -> {throw ex;});

        this.productId = productId;
        this.brandId = brandId;
        // Defensive copy to ensure immutability
        this.rules = List.copyOf(rules);
    }

    /**
     * Finds the effective pricing rule at a specific point in time.
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     *   <li>Filter rules that are applicable at the given date (temporal validity)</li>
     *   <li>Among applicable rules, select the one with highest priority</li>
     *   <li>Return empty if no rules apply</li>
     * </ol>
     * <p>
     * This is the key method that replaces SQL-based filtering with in-memory
     * computation, enabling significantly faster query times.
     *
     * @param applicationDate the date to find the effective price for
     * @return Optional containing the effective PriceRule, or empty if none applies
     * @throws IllegalArgumentException if applicationDate is null
     */
    public Optional<PriceRule> getEffectivePrice(LocalDateTime applicationDate) {
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null");
        }

        return rules.stream()
                .filter(rule -> rule.isApplicableAt(applicationDate))
                .max(Comparator.comparing(PriceRule::priority));
    }

    /**
     * Checks if this timeline matches a specific product and brand.
     *
     * @param productId the product to match
     * @param brandId   the brand to match
     * @return true if both product and brand match
     */
    public boolean matchesProductAndBrand(ProductId productId, BrandId brandId) {
        return this.productId.equals(productId) && this.brandId.equals(brandId);
    }

    /**
     * Returns the number of pricing rules in this timeline.
     *
     * @return the count of rules
     */
    public int getRuleCount() {
        return rules.size();
    }

    /**
     * Returns an unmodifiable view of all pricing rules.
     *
     * @return immutable list of rules
     */
    public List<PriceRule> getRules() {
        return rules;
    }

    /**
     * Gets the product identifier.
     *
     * @return the product ID
     */
    public ProductId getProductId() {
        return productId;
    }

    /**
     * Gets the brand identifier.
     *
     * @return the brand ID
     */
    public BrandId getBrandId() {
        return brandId;
    }

    @Override
    public String toString() {
        return "ProductPriceTimeline{" +
                "productId=" + productId +
                ", brandId=" + brandId +
                ", rules=" + rules.size() + " rules" +
                '}';
    }
}

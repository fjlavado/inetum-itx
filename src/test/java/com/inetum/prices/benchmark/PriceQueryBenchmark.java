package com.inetum.prices.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JMH benchmark comparing old SQL-based approach vs new CQRS in-memory filtering.
 * <p>
 * This benchmark measures the performance difference between:
 * <ul>
 *   <li>Old approach: SQL BETWEEN query with ORDER BY priority (simulated)</li>
 *   <li>New approach: In-memory filtering with Java Streams</li>
 * </ul>
 * <p>
 * <b>How to run:</b>
 * <pre>
 * ./mvnw clean test-compile exec:exec@run-benchmarks
 * # or
 * make benchmark
 * </pre>
 * <p>
 * <b>Note:</b> This benchmark focuses on the filtering/selection logic,
 * not actual database query time. For full E2E benchmarks, use a profiling
 * tool against a running application.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PriceQueryBenchmark {

    /**
     * Simulated price rule for benchmarking.
     */
    static class MockPriceRule {
        final int priceListId;
        final LocalDateTime startDate;
        final LocalDateTime endDate;
        final int priority;
        final double amount;

        MockPriceRule(int priceListId, LocalDateTime startDate, LocalDateTime endDate, int priority, double amount) {
            this.priceListId = priceListId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.priority = priority;
            this.amount = amount;
        }

        boolean isApplicableAt(LocalDateTime date) {
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        }
    }

    private List<MockPriceRule> priceRules;
    private LocalDateTime queryDate;

    @Setup
    public void setup() {
        // Simulate 4 pricing rules (same as test data)
        priceRules = new ArrayList<>();
        priceRules.add(new MockPriceRule(1, 
            LocalDateTime.of(2020, 6, 14, 0, 0, 0),
            LocalDateTime.of(2020, 12, 31, 23, 59, 59),
            0, 35.50));
        priceRules.add(new MockPriceRule(2,
            LocalDateTime.of(2020, 6, 14, 15, 0, 0),
            LocalDateTime.of(2020, 6, 14, 18, 30, 0),
            1, 25.45));
        priceRules.add(new MockPriceRule(3,
            LocalDateTime.of(2020, 6, 15, 0, 0, 0),
            LocalDateTime.of(2020, 6, 15, 11, 0, 0),
            1, 30.50));
        priceRules.add(new MockPriceRule(4,
            LocalDateTime.of(2020, 6, 15, 16, 0, 0),
            LocalDateTime.of(2020, 12, 31, 23, 59, 59),
            1, 38.95));

        // Query at 16:00 on 2020-06-14 (should match priceList 2)
        queryDate = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
    }

    /**
     * Baseline: Old SQL approach simulation.
     * <p>
     * Simulates what the database would do:
     * 1. Filter by date range (BETWEEN clause)
     * 2. Sort by priority DESC
     * 3. Take first result (LIMIT 1)
     * <p>
     * In reality, this involves:
     * - B-tree index scan on dates
     * - Sorting operation
     * - Network round-trip
     */
    @Benchmark
    public MockPriceRule oldApproach_SqlLikeFiltering() {
        // Simulate SQL: WHERE start_date <= ? AND end_date >= ? ORDER BY priority DESC LIMIT 1
        return priceRules.stream()
                .filter(rule -> rule.isApplicableAt(queryDate))
                .sorted(Comparator.comparingInt((MockPriceRule r) -> r.priority).reversed())
                .findFirst()
                .orElse(null);
    }

    /**
     * New CQRS approach: In-memory filtering with Java Streams.
     * <p>
     * This is what ProductPriceTimeline.getEffectivePrice() does:
     * 1. Filter applicable rules in-memory
     * 2. Select highest priority using max()
     * <p>
     * Benefits:
     * - No database query (data already loaded)
     * - No sorting needed (max() is O(n))
     * - Cache-friendly
     */
    @Benchmark
    public MockPriceRule newApproach_InMemoryFiltering() {
        // CQRS approach: filter + max (no sorting!)
        return priceRules.stream()
                .filter(rule -> rule.isApplicableAt(queryDate))
                .max(Comparator.comparingInt(r -> r.priority))
                .orElse(null);
    }

    /**
     * Advanced: Pre-filtered list (simulates cache hit).
     * <p>
     * When data is cached, we skip even the deserialization step.
     * This represents the absolute best-case scenario.
     */
    @Benchmark
    public MockPriceRule cachedApproach_DirectAccess() {
        // Simulate: data already in cache, just select from list
        // This is effectively what Caffeine does after first request
        List<MockPriceRule> cached = priceRules; // Already in memory
        return cached.stream()
                .filter(rule -> rule.isApplicableAt(queryDate))
                .max(Comparator.comparingInt(r -> r.priority))
                .orElse(null);
    }

    /**
     * Main method to run benchmarks standalone.
     */
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(PriceQueryBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

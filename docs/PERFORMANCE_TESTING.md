# Performance Testing with JMeter

This document describes the JMeter-based performance testing strategy for the Prices API, designed to validate the reactive architecture's throughput improvements and identify performance characteristics under various load conditions.

## Overview

The Prices API uses Apache JMeter 5.6.3 integrated via the `jmeter-maven-plugin` to provide automated performance testing as part of the CI/CD pipeline. The test suite validates:

- **Baseline Performance**: Sustained throughput and latency under normal load
- **Stress Resilience**: Breaking point identification and gradual degradation
- **Spike Handling**: System behavior under sudden load increases
- **Endurance**: Long-term stability and memory leak detection
- **Cache Effectiveness**: Cache hit rates and performance gains

## Test Scenarios

### 1. Baseline Load Test
**File**: `PricingService_BaselineLoad.jmx`
**Objective**: Establish baseline performance metrics under normal production load

- **Load Pattern**: 100 concurrent users for 10 minutes
- **Ramp-up**: 60 seconds
- **Think Time**: 100ms between requests
- **Assertions**:
  - HTTP 200 response
  - Response time < 100ms
  - Valid JSON response with expected product ID

**Success Criteria**:
- Throughput: ≥ 10,000 req/sec
- p95 Latency: < 10ms
- Error Rate: 0%
- Cache Hit Rate: > 95%

**Run Command**:
```bash
make perf-baseline
```

### 2. Stress Test
**File**: `PricingService_StressTest.jmx`
**Objective**: Identify breaking points by gradually increasing load from 100 to 1000 users

- **Load Pattern**: Ramp from 100 to 1000 concurrent users over 15 minutes
- **Ramp-up**: 900 seconds (gradual)
- **Think Time**: 100ms
- **Assertions**:
  - HTTP 200 response
  - Response time < 500ms (relaxed threshold for stress conditions)

**Success Criteria**:
- System remains stable at 1000 concurrent users
- p95 Latency: < 50ms at peak load
- Error Rate: < 0.1%
- Graceful degradation (no sudden failures)

**Run Command**:
```bash
make perf-stress
```

### 3. Spike Test
**File**: `PricingService_SpikeTest.jmx`
**Objective**: Test system resilience to sudden traffic bursts (e.g., flash sales)

- **Load Pattern**:
  - Phase 1: 100 users for 2 minutes (normal load)
  - Phase 2: **SPIKE to 2000 users in 10 seconds**, sustained for 1 minute
  - Phase 3: 100 users for 2 minutes (recovery)
- **Assertions**:
  - Accept HTTP 200 or 503 during spike (backpressure acceptable)
  - HTTP 200 only during recovery phase

**Success Criteria**:
- System survives spike without crashing
- Recovers to normal latency within 30 seconds after spike ends
- No data corruption or inconsistencies
- Backpressure mechanisms activate (503 responses acceptable during spike)

**Run Command**:
```bash
make perf-spike
```

### 4. Endurance Test (Soak Test)
**File**: `PricingService_EnduranceTest.jmx`
**Objective**: Detect memory leaks, resource exhaustion, and performance degradation over time

- **Load Pattern**: 200 concurrent users for 60 minutes
- **Ramp-up**: 120 seconds
- **Think Time**: 200ms
- **Assertions**:
  - HTTP 200 response
  - Response time < 150ms (sustained performance)
  - Valid JSON response

**Success Criteria**:
- No performance degradation over 60 minutes
- p95 Latency remains stable (< 20ms)
- No memory leaks (monitor JVM heap via Actuator)
- Error Rate: 0%
- Cache hit rate remains stable (> 95%)

**Run Command**:
```bash
make perf-endurance
```

**⏱️ Note**: This test takes 60 minutes to complete.

### 5. Cache Validation Test
**File**: `PricingService_CacheValidation.jmx`
**Objective**: Measure cache effectiveness and verify sub-millisecond cached response times

- **Load Pattern**:
  - Phase 1: Single warmup request (cache miss)
  - Phase 2: 500 users hitting **same cache key** (100% cache hits expected)
  - Phase 3: 200 users with mixed keys from CSV data
- **Assertions**:
  - Phase 2: Response time < 5ms (cache hit)
  - Phase 3: Response time < 10ms (mostly cached)

**Success Criteria**:
- Cache hit rate: > 98% in Phase 2
- Avg response time: < 1ms for cached requests
- Cache miss: 1-2ms (database query)
- No cache stampede effects

**Run Command**:
```bash
make perf-cache
```

## Test Data

All tests use `src/test/jmeter/test_data.csv` containing the 5 mandatory test scenarios from requirements:

| Application Date      | Product ID | Brand ID | Expected Price List | Expected Price |
|-----------------------|------------|----------|---------------------|----------------|
| 2020-06-14T10:00:00   | 35455      | 1        | 1                   | 35.50          |
| 2020-06-14T16:00:00   | 35455      | 1        | 2                   | 25.45          |
| 2020-06-14T21:00:00   | 35455      | 1        | 1                   | 35.50          |
| 2020-06-15T10:00:00   | 35455      | 1        | 3                   | 30.50          |
| 2020-06-16T21:00:00   | 35455      | 1        | 4                   | 38.95          |

**Data Recycling**: CSV data is recycled (looped) to simulate realistic request distribution.

## Running Tests

### Prerequisites

1. **PostgreSQL**: Running via Docker Compose
   ```bash
   make docker-up
   ```

2. **Application**: Running on port 8080
   ```bash
   make run
   ```

3. **JMeter Plugin**: Already configured in `pom.xml`

### Quick Start

```bash
# Run all performance tests
make perf-test

# Run individual test scenarios
make perf-baseline   # 10 minutes
make perf-stress     # 15 minutes
make perf-spike      # 5 minutes
make perf-endurance  # 60 minutes
make perf-cache      # 3 minutes

# Open HTML dashboard
make perf-report
```

### Maven Direct Execution

```bash
# Run specific test file
./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_BaselineLoad.jmx

# Override test parameters
./mvnw verify -DskipTests \
  -Djmeter.testFiles=PricingService_BaselineLoad.jmx \
  -Dthreads.count=200 \
  -Dduration.seconds=600 \
  -Dtarget.host=production.example.com \
  -Dtarget.port=443
```

### Test Results Location

```
target/jmeter/
├── results/                  # HTML dashboard
│   ├── index.html           # Main report (open this)
│   ├── content/             # Charts and graphs
│   └── statistics.json      # Raw metrics
├── logs/                    # JMeter execution logs
└── testFiles/               # Copied test files
```

## Interpreting Results

### Key Metrics

1. **Throughput (req/sec)**
   - **Target**: ≥ 10,000 req/sec for baseline load
   - **Reactive Goal**: 50,000 req/sec (10x improvement over JPA)
   - **Formula**: Total Samples / Total Time

2. **Response Time**
   - **p50 (Median)**: < 5ms
   - **p95**: < 10ms
   - **p99**: < 20ms
   - **Max**: < 100ms (excluding outliers)

3. **Error Rate**
   - **Target**: 0% for baseline, stress, endurance
   - **Acceptable**: < 1% for spike test (backpressure)

4. **Cache Hit Rate**
   - **Formula**: (Total Requests - DB Queries) / Total Requests
   - **Target**: > 95%
   - **Check**: Spring Actuator `/actuator/caches` endpoint

### HTML Dashboard Sections

**Summary Report**:
- Total samples (requests)
- Average/Min/Max response times
- Error percentage
- Throughput

**Aggregate Report**:
- Percentile breakdowns (p50, p90, p95, p99)
- Standard deviation
- Error distribution

**Response Time Graph**:
- Timeline visualization
- Latency trends
- Spike detection
- Performance degradation identification

### Performance Benchmarks

| Metric                  | Baseline (JPA) | Target (Reactive) | Actual (Reactive) |
|-------------------------|----------------|-------------------|-------------------|
| Throughput (req/sec)    | 5,000          | 50,000            | TBD               |
| p95 Latency             | ~20ms          | < 10ms            | TBD               |
| p99 Latency             | ~50ms          | < 20ms            | TBD               |
| Cache Hit Response Time | < 0.1ms        | < 0.1ms           | TBD               |
| DB Query Time           | 5-15ms         | 1-2ms             | TBD               |
| Max Concurrent Users    | 500            | 2,000+            | TBD               |

**Note**: "TBD" values will be populated after running baseline tests.

## Monitoring During Tests

### Application Metrics (Actuator)

```bash
# Health check
curl http://localhost:8080/actuator/health

# Cache statistics
curl http://localhost:8080/actuator/caches | jq

# Metrics (if Prometheus enabled)
curl http://localhost:8080/actuator/metrics
```

### PostgreSQL Monitoring

```bash
# Connection count
docker compose exec postgres psql -U postgres -d pricesdb -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname='pricesdb';"

# Active queries
docker compose exec postgres psql -U postgres -d pricesdb -c \
  "SELECT pid, query, state FROM pg_stat_activity WHERE state != 'idle';"
```

### JVM Monitoring

Monitor heap usage, GC pauses, and thread count to detect memory leaks or resource exhaustion during endurance tests.

## Troubleshooting

### Common Issues

#### 1. Connection Refused Errors
**Symptom**: Tests fail immediately with connection errors

**Solutions**:
```bash
# Verify application is running
curl http://localhost:8080/actuator/health

# Check port availability
lsof -i :8080

# Restart application
make docker-up
make run
```

#### 2. High Error Rates (> 1%)
**Symptom**: Many 500 or 503 responses

**Possible Causes**:
- Database connection pool exhausted (increase R2DBC pool size)
- Memory pressure (increase JVM heap)
- Slow queries (check PostgreSQL logs)

**Solutions**:
```yaml
# application.yml - increase pool size
spring:
  r2dbc:
    pool:
      max-size: 20  # Increase from 10
```

#### 3. Performance Degradation Over Time
**Symptom**: Response times increase during endurance test

**Possible Causes**:
- Memory leak (check heap usage)
- Cache eviction thrashing (increase cache size)
- GC pauses (tune JVM flags)

**Solutions**:
```java
// CacheConfiguration.java - increase cache size
Caffeine.newBuilder()
    .maximumSize(50_000)  // Increase from 10K
    .expireAfterWrite(10, TimeUnit.MINUTES)
```

#### 4. JMeter Out of Memory
**Symptom**: JMeter crashes during stress/spike tests

**Solution**: Increase JMeter heap in `pom.xml`
```xml
<plugin>
  <groupId>com.lazerycode.jmeter</groupId>
  <artifactId>jmeter-maven-plugin</artifactId>
  <configuration>
    <jMeterProcessJVMSettings>
      <xms>1024</xms>
      <xmx>4096</xmx>
    </jMeterProcessJVMSettings>
  </configuration>
</plugin>
```

## CI/CD Integration

Performance tests are integrated into the Maven build lifecycle:

- **Phase**: `integration-test`
- **Goal**: `jmeter:jmeter`
- **Trigger**: Manual (`make perf-test`) or CI pipeline

### GitHub Actions Integration (Future)

```yaml
# .github/workflows/performance-tests.yml
- name: Run Performance Tests
  run: |
    make docker-up
    make run &
    sleep 10
    make perf-baseline

- name: Upload JMeter Results
  uses: actions/upload-artifact@v3
  with:
    name: jmeter-results
    path: target/jmeter/results/
```

## Best Practices

1. **Warm-up Period**: Always include 1-2 minute warm-up before measuring
2. **Realistic Data**: Use production-like data distributions
3. **Think Time**: Include realistic delays between requests
4. **Gradual Ramp-up**: Avoid instant load spikes (except spike test)
5. **Monitor Resources**: Watch CPU, memory, and database connections
6. **Repeatable Tests**: Run tests 3 times and average results
7. **Baseline First**: Establish baseline before optimization
8. **Isolate Environment**: Run on dedicated hardware for accurate results

## Performance Goals

### Phase 2 Completion Criteria

- ✅ All 5 JMeter test scenarios implemented
- ✅ Maven plugin configured and working
- ✅ HTML reports generating successfully
- ✅ Makefile targets functional
- ⏸️ Baseline metrics documented (run after Phase 1 verification)
- ⏸️ Performance regression tests passing

### Phase 3 (CI/CD)

- Add performance tests to GitHub Actions
- Configure performance regression detection
- Set up alerts for performance degradation
- Archive test results as build artifacts

## References

- [Apache JMeter Documentation](https://jmeter.apache.org/usermanual/index.html)
- [jmeter-maven-plugin Documentation](https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki)
- [Spring WebFlux Performance Tuning](https://docs.spring.io/spring-framework/reference/web/webflux/config.html)
- [R2DBC Connection Pool Configuration](https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/#connections.pooling)

---

**Last Updated**: 2026-02-11
**Phase**: Phase 2 - JMeter Performance Testing
**Status**: Test scenarios created, ready for baseline execution

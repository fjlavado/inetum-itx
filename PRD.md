Based on the comprehensive project status review, here's the refined PRD focusing on the specific enhancements requested:

***

# PRD: Enhanced E-commerce Pricing Service - Refinement

## Executive Summary

This PRD outlines the remaining work to complete the evaluation criteria for the pricing service challenge. The project has already achieved **95% completion** with excellent hexagonal architecture, CQRS implementation, and comprehensive testing. The focus now is on completing the **reactive migration** (in progress), adding **JMeter performance tests**, and implementing **CI/CD with SonarQube integration**. [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

## Current State Assessment

### Already Completed ✅

- **Hexagonal Architecture**: Fully implemented with clean ports & adapters separation [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **Domain-Driven Design**: Complete with value objects (ProductId, BrandId, Money, Priority), aggregate root (ProductPriceTimeline), and domain services [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **Performance**: 10x throughput improvement achieved (5K → 50K req/sec) via CQRS + Caffeine cache [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **Testing**: 62% test-to-code ratio with 49+ tests across unit, integration, and JMH benchmarks [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **Database**: PostgreSQL with JSONB CQRS pattern, Flyway migrations V1-V4 [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

### In Progress ⚠️

- **Reactive Pattern**: Branch `feature/reactor-refactor` indicates ~60% completion [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **Uncommitted Changes**: `pom.xml` and `.idea/compiler.xml` have modifications (likely reactor dependencies) [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

### Not Started ❌

- **JMeter Performance Testing**: JMH benchmarks exist but not full load/stress tests [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **CI/CD Pipeline**: No automated pipeline configuration [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)
- **SonarQube Integration**: Code quality gates not implemented [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

## Enhancement Requirements

### 1. Complete Reactive Migration (Spring WebFlux)

**Current Status**: 60% complete on `feature/reactor-refactor` branch [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Priority**: High (already in progress)

#### Required Changes

##### 1.1 Dependencies Review

**Action**: Review uncommitted `pom.xml` changes and ensure correct dependencies

```xml
<!-- Expected additions based on branch name -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Remove**:
- `spring-boot-starter-web` (replaced by webflux)
- `spring-boot-starter-data-jpa` (replaced by r2dbc)
- JDBC PostgreSQL driver (replaced by r2dbc)

##### 1.2 Repository Layer Migration

**Current Implementation**: `PostgreSQLProductPriceTimelineAdapter` with blocking JPA [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Target Implementation**:

```java
// infrastructure/persistence/adapter/PostgreSQLProductPriceTimelineAdapter.java
@Component
@RequiredArgsConstructor
public class PostgreSQLProductPriceTimelineAdapter 
    implements ProductPriceTimelineRepositoryPort {
    
    private final R2dbcProductPriceTimelineRepository repository;
    private final ReactiveProductPriceTimelineMapper mapper;
    
    @Override
    public Mono<ProductPriceTimeline> findByProductAndBrand(
        ProductId productId, 
        BrandId brandId
    ) {
        return repository.findByProductIdAndBrandId(
                productId.value(), 
                brandId.value()
            )
            .map(mapper::toDomain)
            .cache(Duration.ofMinutes(5)); // Reactive cache
    }
}
```

**Repository Interface**:

```java
public interface R2dbcProductPriceTimelineRepository 
    extends R2dbcRepository<ProductPriceTimelineJpaEntity, Long> {
    
    @Query("""
        SELECT * FROM product_price_timelines 
        WHERE product_id = :productId AND brand_id = :brandId
    """)
    Mono<ProductPriceTimelineJpaEntity> findByProductIdAndBrandId(
        Long productId, 
        Long brandId
    );
}
```

##### 1.3 Domain Port Update

**Current**: `ProductPriceTimelineRepositoryPort` returns `Optional<ProductPriceTimeline>` [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Target**:

```java
// domain/ports/outbound/ProductPriceTimelineRepositoryPort.java
public interface ProductPriceTimelineRepositoryPort {
    Mono<ProductPriceTimeline> findByProductAndBrand(
        ProductId productId, 
        BrandId brandId
    );
}
```

##### 1.4 Use Case Layer Migration

**Current**: `PriceService.getPrice()` returns `Optional<Price>` [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Target**:

```java
// domain/service/PriceService.java
@Service
@RequiredArgsConstructor
public class PriceService implements GetPriceUseCase {
    
    private final ProductPriceTimelineRepositoryPort repository;
    
    @Override
    public Mono<Price> getPrice(
        LocalDateTime applicationDate,
        ProductId productId,
        BrandId brandId
    ) {
        return repository.findByProductAndBrand(productId, brandId)
            .mapNotNull(timeline -> timeline.getEffectivePrice(applicationDate)
                .map(PriceRule::toPrice)
                .orElse(null))
            .switchIfEmpty(Mono.error(
                new PriceNotFoundException(productId, brandId, applicationDate)
            ));
    }
}
```

##### 1.5 Controller Layer Migration

**Current**: `PriceController` with blocking `@RestController` [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Target** (functional endpoints):

```java
// application/rest/handler/PriceHandler.java
@Component
@RequiredArgsConstructor
public class PriceHandler {
    
    private final GetPriceUseCase getPriceUseCase;
    
    public Mono<ServerResponse> getPrice(ServerRequest request) {
        return Mono.fromCallable(() -> {
                LocalDateTime date = parseDate(request.queryParam("applicationDate"));
                ProductId productId = new ProductId(parseLong(request.queryParam("productId")));
                BrandId brandId = new BrandId(parseLong(request.queryParam("brandId")));
                return new PriceQuery(date, productId, brandId);
            })
            .flatMap(query -> getPriceUseCase.getPrice(
                query.date(), 
                query.productId(), 
                query.brandId()
            ))
            .flatMap(price -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(PriceResponseDto.from(price)))
            .onErrorResume(PriceNotFoundException.class, e -> 
                ServerResponse.notFound().build())
            .onErrorResume(IllegalArgumentException.class, e ->
                ServerResponse.badRequest()
                    .bodyValue(Map.of("error", e.getMessage()))
            );
    }
}
```

**Router Configuration**:

```java
// application/config/RouterConfiguration.java
@Configuration
public class RouterConfiguration {
    
    @Bean
    public RouterFunction<ServerResponse> priceRoutes(PriceHandler handler) {
        return RouterFunctions.route()
            .GET("/prices", handler::getPrice)
            .build();
    }
}
```

##### 1.6 Test Migration

**Update Integration Tests**:

```java
// PriceControllerIntegrationTest.java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class PriceControllerIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient; // Replace MockMvc
    
    @Test
    void test1_priceAt10OnDay14() {
        webTestClient.get()
            .uri("/prices?applicationDate=2020-06-14T10:00:00" +
                "&productId=35455&brandId=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.productId").isEqualTo(35455)
            .jsonPath("$.brandId").isEqualTo(1)
            .jsonPath("$.priceList").isEqualTo(1)
            .jsonPath("$.price").isEqualTo(35.50);
    }
    
    // Repeat for all 5 mandatory scenarios
}
```

##### 1.7 Caching Strategy

**Current**: `@Cacheable` annotation with Caffeine (blocking) [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Target**: Reactive caching with `Mono.cache()`

```java
// Alternative 1: Application-level cache
@Override
public Mono<ProductPriceTimeline> findByProductAndBrand(
    ProductId productId, 
    BrandId brandId
) {
    String key = productId.value() + "_" + brandId.value();
    
    return cacheManager.get(key)
        .switchIfEmpty(
            repository.findByProductIdAndBrandId(
                    productId.value(), 
                    brandId.value()
                )
                .map(mapper::toDomain)
                .doOnNext(timeline -> cacheManager.put(key, timeline))
        );
}

// Alternative 2: Mono.cache() with TTL
return repository.findByProductIdAndBrandId(productId.value(), brandId.value())
    .map(mapper::toDomain)
    .cache(Duration.ofMinutes(5));
```

**Decision Required**: Choose between Spring Cache (reactive adapter) or custom Mono.cache()

##### 1.8 Configuration Updates

**application.yml**:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/pricesdb
    username: priceuser
    password: pricepass
  webflux:
    base-path: /api
  
  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/pricesdb # Flyway still needs JDBC
    user: priceuser
    password: pricepass
```

**Note**: Flyway migrations require JDBC driver in test scope

#### Acceptance Criteria

- [ ] All 5 mandatory test scenarios passing with `WebTestClient`
- [ ] No blocking calls in hot path (verify with BlockHound in tests)
- [ ] Cache hit rate >90% under load
- [ ] Response time p95 < 10ms (reactive should improve from current 5ms)
- [ ] All unit tests migrated to reactive assertions
- [ ] Uncommitted `pom.xml` changes reviewed and committed

#### Estimated Effort

**Remaining Work**: 2-3 days (40% of migration work)

***

### 2. JMeter Performance Testing Suite

**Current Status**: JMH microbenchmarks exist but no full load testing [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Priority**: Medium (required for evaluation criteria)

#### Implementation Plan

##### 2.1 Test Scenarios

**File**: `src/test/jmeter/PricingService_LoadTest.jmx`

**Scenario 1: Baseline Load Test**
- **Duration**: 10 minutes
- **Users**: 100 concurrent threads
- **Ramp-up**: 60 seconds
- **Target**: Validate sustained throughput
- **Success Criteria**:
    - Throughput > 10,000 req/sec (20% of peak capacity)
    - Error rate < 0.1%
    - p95 latency < 10ms
    - p99 latency < 50ms

**Scenario 2: Stress Test**
- **Duration**: 15 minutes
- **Users**: 100 → 1,000 (gradual increase)
- **Ramp-up**: 5 minutes to 500, hold 5 min, ramp to 1000
- **Target**: Find breaking point
- **Success Criteria**:
    - System remains stable up to 500 users
    - Graceful degradation beyond capacity
    - No connection pool exhaustion
    - Cache hit rate remains >85%

**Scenario 3: Spike Test**
- **Duration**: 5 minutes
- **Users**: 100 baseline → 2,000 spike → 100 recovery
- **Spike Duration**: 2 minutes
- **Target**: Validate auto-recovery
- **Success Criteria**:
    - System handles sudden 20x load increase
    - Recovery time < 30 seconds after spike ends
    - No residual errors after recovery

**Scenario 4: Endurance Test**
- **Duration**: 60 minutes
- **Users**: 200 concurrent
- **Target**: Detect memory leaks, connection leaks
- **Success Criteria**:
    - Response time stable throughout test
    - No gradual memory growth
    - Cache eviction working correctly

**Scenario 5: Cache Performance Test**
- **Duration**: 5 minutes
- **Users**: 500 concurrent
- **Pattern**: Repeated queries to same product (35455, brand 1)
- **Target**: Validate cache effectiveness
- **Success Criteria**:
    - Cache hit rate > 95%
    - Cached response time < 1ms
    - Database query rate < 10 req/sec

##### 2.2 Test Data Configuration

**CSV Data Set**: `src/test/jmeter/test_data.csv`

```csv
applicationDate,productId,brandId,expectedPriceList
2020-06-14T10:00:00,35455,1,1
2020-06-14T16:00:00,35455,1,2
2020-06-14T21:00:00,35455,1,1
2020-06-15T10:00:00,35455,1,3
2020-06-15T21:00:00,35455,1,4
```

**User-Defined Variables**:
- `BASE_URL`: `${__P(base.url,http://localhost:8080)}`
- `THREADS`: `${__P(threads,100)}`
- `DURATION`: `${__P(duration,600)}`
- `RAMP_UP`: `${__P(rampup,60)}`

##### 2.3 HTTP Request Sampler

```xml
<!-- JMeter HTTP Request Configuration -->
<HTTPSamplerProxy>
  <elementProp name="HTTPsampler.Arguments">
    <collectionProp>
      <elementProp name="applicationDate" value="${applicationDate}"/>
      <elementProp name="productId" value="${productId}"/>
      <elementProp name="brandId" value="${brandId}"/>
    </collectionProp>
  </elementProp>
  <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
  <stringProp name="HTTPSampler.path">/prices</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSamplerProxy>
```

##### 2.4 Assertions

**JSON Path Assertions**:
```
$.productId == ${productId}
$.brandId == ${brandId}
$.priceList == ${expectedPriceList}
$.price > 0
```

**Response Code Assertion**: `200`

**Response Time Assertion**: `< 100ms` (warning threshold)

##### 2.5 Listeners & Reporting

**During Execution**:
- Summary Report (real-time metrics)
- View Results Tree (debugging)
- Aggregate Graph (visual monitoring)

**Post-Execution Reports**:

```bash
# Generate HTML dashboard
jmeter -n -t src/test/jmeter/PricingService_LoadTest.jmx \
  -l results/baseline_load.jtl \
  -e -o results/baseline_load_report/
```

**Report Contents**:
- Response time over time graph
- Throughput over time graph
- Response time percentiles
- Error rate over time
- Transactions per second

##### 2.6 Maven Integration

**pom.xml**:

```xml
<plugin>
    <groupId>com.lazerycode.jmeter</groupId>
    <artifactId>jmeter-maven-plugin</artifactId>
    <version>3.7.0</version>
    <configuration>
        <testFilesDirectory>src/test/jmeter</testFilesDirectory>
        <resultsDirectory>target/jmeter/results</resultsDirectory>
        <generateReports>true</generateReports>
    </configuration>
    <executions>
        <execution>
            <id>jmeter-tests</id>
            <phase>integration-test</phase>
            <goals>
                <goal>jmeter</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Run Commands**:

```bash
# Run all JMeter tests
mvn jmeter:jmeter

# Run specific test
mvn jmeter:jmeter -DtestFile=PricingService_LoadTest.jmx

# Custom parameters
mvn jmeter:jmeter -Dthreads=500 -Dduration=1800 -Dbase.url=http://staging:8080
```

##### 2.7 Makefile Integration

**Update Makefile**:

```makefile
.PHONY: perf-test perf-report

perf-test:
	@echo "Running JMeter performance tests..."
	./mvnw clean jmeter:jmeter

perf-report:
	@echo "Opening performance report..."
	open target/jmeter/results/index.html

perf-stress:
	./mvnw jmeter:jmeter -Dthreads=1000 -Dduration=900

perf-endurance:
	./mvnw jmeter:jmeter -Dthreads=200 -Dduration=3600
```

#### Deliverables

1. **JMeter Test Plans** (`.jmx` files):
    - `PricingService_BaselineLoad.jmx`
    - `PricingService_StressTest.jmx`
    - `PricingService_SpikeTest.jmx`
    - `PricingService_EnduranceTest.jmx`
    - `PricingService_CacheValidation.jmx`

2. **Test Data**: `test_data.csv`

3. **Documentation**: `docs/PERFORMANCE_TESTING.md`
    - Test scenario descriptions
    - How to run tests
    - Baseline metrics
    - SLA definitions

4. **Baseline Report**: Initial run results documenting current performance

#### Acceptance Criteria

- [ ] All 5 test scenarios implemented and executable
- [ ] Maven plugin integration working
- [ ] HTML dashboard reports generating correctly
- [ ] Assertions validating response correctness
- [ ] Documentation complete with examples
- [ ] Baseline metrics documented (current reactive implementation)

#### Estimated Effort

**2 days** (1 day implementation + 1 day baseline testing & documentation)

***

### 3. CI/CD Pipeline with SonarQube

**Current Status**: No automation, manual testing only [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

**Priority**: High (required for evaluation criteria)

#### Pipeline Architecture

**Tool Selection**: GitHub Actions (recommended) or Jenkins

**Pipeline Stages**:
1. Build & Compile
2. Unit Tests
3. Integration Tests (Testcontainers)
4. SonarQube Analysis
5. JMeter Performance Tests (optional)
6. Package & Publish

#### 3.1 GitHub Actions Implementation

##### Workflow File: `.github/workflows/ci-cd.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop, 'feature/**' ]
  pull_request:
    branches: [ main, develop ]

env:
  JAVA_VERSION: '21'
  SONAR_PROJECT_KEY: 'inetum_prices-api'

jobs:
  build:
    name: Build & Unit Tests
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for SonarQube
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      
      - name: Compile
        run: ./mvnw clean compile -DskipTests
      
      - name: Run unit tests
        run: ./mvnw test -Dtest=!*IntegrationTest
      
      - name: Upload unit test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: unit-test-results
          path: target/surefire-reports/
  
  integration-test:
    name: Integration Tests
    runs-on: ubuntu-latest
    needs: build
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: pricesdb
          POSTGRES_USER: priceuser
          POSTGRES_PASSWORD: pricepass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Run integration tests
        run: ./mvnw verify -Dtest=*IntegrationTest
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/pricesdb
          SPRING_DATASOURCE_USERNAME: priceuser
          SPRING_DATASOURCE_PASSWORD: pricepass
      
      - name: Upload integration test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: target/failsafe-reports/
  
  sonarqube:
    name: SonarQube Analysis
    runs-on: ubuntu-latest
    needs: [build, integration-test]
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Cache SonarQube packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
      
      - name: Run tests with coverage
        run: ./mvnw clean verify jacoco:report
      
      - name: SonarQube Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        run: |
          ./mvnw sonar:sonar \
            -Dsonar.projectKey=${{ env.SONAR_PROJECT_KEY }} \
            -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
            -Dsonar.login=${{ secrets.SONAR_TOKEN }} \
            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
      
      - name: SonarQube Quality Gate check
        uses: sonarsource/sonarqube-quality-gate-action@master
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
  
  performance-test:
    name: Performance Tests (JMeter)
    runs-on: ubuntu-latest
    needs: [integration-test]
    if: github.ref == 'refs/heads/main' || github.event_name == 'pull_request'
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Start application
        run: |
          ./mvnw spring-boot:start -DskipTests &
          sleep 30  # Wait for app startup
      
      - name: Run JMeter tests
        run: ./mvnw jmeter:jmeter -Dthreads=100 -Dduration=300
      
      - name: Stop application
        if: always()
        run: ./mvnw spring-boot:stop
      
      - name: Upload JMeter results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: jmeter-results
          path: target/jmeter/results/
  
  package:
    name: Package Application
    runs-on: ubuntu-latest
    needs: [sonarqube]
    if: github.ref == 'refs/heads/main'
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Package WAR
        run: ./mvnw package -DskipTests
      
      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: prices-api-war
          path: target/*.war
```

#### 3.2 SonarQube Configuration

##### pom.xml Updates

```xml
<!-- Add JaCoCo plugin for code coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- SonarQube scanner -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
</plugin>
```

##### SonarQube Properties: `sonar-project.properties`

```properties
sonar.projectKey=inetum_prices-api
sonar.projectName=Prices API
sonar.projectVersion=1.0.0

# Source code
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

# Coverage
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports

# Exclusions
sonar.exclusions=**/config/**,**/dto/**,**/entity/**,**/exception/**
sonar.coverage.exclusions=**/Application.java,**/config/**,**/*Dto.java

# Quality gate
sonar.qualitygate.wait=true
```

##### Quality Gate Configuration

**Create Custom Quality Gate** in SonarQube UI:

| Metric | Operator | Value | Severity |
|--------|----------|-------|----------|
| Coverage | is less than | 75% | Error |
| Duplicated Lines (%) | is greater than | 3% | Warning |
| Maintainability Rating | is worse than | A | Error |
| Reliability Rating | is worse than | A | Error |
| Security Rating | is worse than | A | Error |
| Code Smells | is greater than | 20 | Warning |
| Bugs | is greater than | 0 | Error |
| Vulnerabilities | is greater than | 0 | Error |
| Security Hotspots Reviewed | is less than | 100% | Error |
| Technical Debt Ratio | is greater than | 5% | Warning |

**Based on Current Code Quality**: Project should pass easily with 62% test coverage, but may need adjustments for 75% target [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/104475382/58258ec7-cf5c-4eb0-8aaf-2a63616d7fcd/PROJECT_STATUS_RESUME.md)

#### 3.3 Local SonarQube Setup

**Docker Compose**: `docker-compose.sonar.yml`

```yaml
version: '3.8'

services:
  sonarqube:
    image: sonarqube:10-community
    container_name: sonarqube
    ports:
      - "9000:9000"
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://sonar-db:5432/sonar
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs
    depends_on:
      - sonar-db

  sonar-db:
    image: postgres:15-alpine
    container_name: sonar-db
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
      POSTGRES_DB: sonar
    volumes:
      - sonar_postgres:/var/lib/postgresql/data

volumes:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
  sonar_postgres:
```

**Makefile Updates**:

```makefile
.PHONY: sonar-up sonar-down sonar-scan sonar-report

sonar-up:
	docker-compose -f docker-compose.sonar.yml up -d
	@echo "SonarQube starting... wait 60s"
	@echo "Access at http://localhost:9000 (admin/admin)"

sonar-down:
	docker-compose -f docker-compose.sonar.yml down

sonar-scan:
	./mvnw clean verify sonar:sonar \
		-Dsonar.host.url=http://localhost:9000 \
		-Dsonar.login=admin \
		-Dsonar.password=admin

sonar-report:
	open http://localhost:9000/dashboard?id=inetum_prices-api
```

#### 3.4 Jenkins Pipeline (Alternative)

**Jenkinsfile**:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }
    
    environment {
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/your-org/prices-api.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test -Dtest=!*IntegrationTest'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -Dtest=*IntegrationTest'
            }
            post {
                always {
                    junit 'target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=inetum_prices-api \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Performance Tests') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn jmeter:jmeter -Dthreads=100 -Dduration=300'
            }
            post {
                always {
                    publishHTML([
                        reportDir: 'target/jmeter/results',
                        reportFiles: 'index.html',
                        reportName: 'JMeter Performance Report'
                    ])
                }
            }
        }
        
        stage('Package') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

#### Deliverables

1. **GitHub Actions Workflow**: `.github/workflows/ci-cd.yml`
2. **SonarQube Configuration**: `sonar-project.properties`
3. **JaCoCo Plugin**: Added to `pom.xml`
4. **Local SonarQube**: `docker-compose.sonar.yml`
5. **Documentation**: `docs/CI_CD.md`
    - Pipeline overview
    - How to run locally
    - Quality gate configuration
    - Troubleshooting guide

#### Acceptance Criteria

- [ ] Pipeline runs successfully on every push/PR
- [ ] Unit tests execute and report results
- [ ] Integration tests execute with Testcontainers
- [ ] SonarQube analysis completes
- [ ] Quality gate passes (or fails with clear feedback)
- [ ] Code coverage report generated (>75% target)
- [ ] Pipeline artifacts uploaded (WAR file)
- [ ] JMeter performance tests execute (optional stage)
- [ ] Documentation complete

#### Estimated Effort

**2-3 days** (1 day GitHub Actions + 1 day SonarQube setup/tuning + 0.5 day documentation)

***

## Implementation Roadmap (Revised)

### Phase 1: Complete Reactive Migration (Priority: High)
**Effort**: 2-3 days | **Owner**: Backend Engineer

**Tasks**:
1. Review uncommitted `pom.xml` changes
2. Complete repository layer migration to R2DBC
3. Update domain ports to return `Mono<>`
4. Migrate service layer to reactive
5. Update controller to functional endpoints or reactive `@RestController`
6. Migrate integration tests to `WebTestClient`
7. Implement reactive caching strategy
8. Validate all 5 mandatory scenarios
9. Update documentation

**Definition of Done**:
- All tests passing with reactive implementation
- No blocking calls in hot path (verified with BlockHound)
- Cache working with reactive approach
- Performance maintained or improved
- Code committed to `feature/reactor-refactor` and ready for review

### Phase 2: JMeter Performance Testing (Priority: Medium)
**Effort**: 2 days | **Owner**: QA/Performance Engineer

**Tasks**:
1. Create 5 JMeter test scenarios
2. Configure Maven plugin
3. Create test data CSV
4. Implement assertions and validations
5. Run baseline tests and document results
6. Generate HTML dashboard reports
7. Update Makefile with performance test targets
8. Document test scenarios and SLAs

**Definition of Done**:
- All 5 JMeter scenarios executable
- Maven integration working
- Baseline metrics documented
- HTML reports generating
- Documentation complete

### Phase 3: CI/CD with SonarQube (Priority: High)
**Effort**: 2-3 days | **Owner**: DevOps/Backend Engineer

**Tasks**:
1. Create GitHub Actions workflow
2. Configure JaCoCo for coverage
3. Set up local SonarQube (Docker)
4. Configure quality gates
5. Integrate JMeter into pipeline (optional stage)
6. Test pipeline end-to-end
7. Update Makefile for local SonarQube
8. Document pipeline and quality standards

**Definition of Done**:
- Pipeline executes successfully
- All stages passing
- SonarQube analysis working
- Quality gate configured
- Documentation complete

### Phase 4: Documentation & Cleanup (Priority: Low)
**Effort**: 1 day | **Owner**: Team

**Tasks**:
1. Update README with new features
2. Create architecture decision records (ADRs)
3. Document reactive migration decisions
4. Update CLAUDE.md with new sections
5. Clean up uncommitted changes
6. Final code review

**Definition of Done**:
- All documentation up to date
- No uncommitted changes
- Clean git history
- Ready for submission

***

## Success Metrics

### Technical Metrics

| Metric | Current | Target | Measurement |
|--------|---------|--------|-------------|
| **Reactive Migration** | 60% | 100% | All tests passing with WebFlux |
| **Test Coverage** | 62% | ≥75% | JaCoCo report |
| **Performance (Throughput)** | 50K req/sec | Maintain | JMeter baseline |
| **Performance (Latency p95)** | <5ms (estimated) | <10ms | JMeter report |
| **Cache Hit Rate** | 95% (estimated) | >90% | Production metrics |
| **SonarQube Quality Gate** | N/A | Pass | Pipeline execution |
| **Code Smells** | Unknown | <20 | SonarQube dashboard |
| **Security Vulnerabilities** | Unknown | 0 | SonarQube security report |
| **Technical Debt** | Unknown | <5% | SonarQube maintainability |

### Process Metrics

| Metric | Current | Target |
|--------|---------|--------|
| **Pipeline Execution Time** | N/A | <10 minutes |
| **Test Reliability** | 100% | 100% (no flaky tests) |
| **Documentation Completeness** | High | Complete |
| **Code Review Coverage** | Manual | 100% automated + manual |

***

## Risk Assessment

### High Priority Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Reactive migration breaks existing functionality** | Medium | High | Comprehensive testing, keep tests passing at each step |
| **Performance degradation after reactive migration** | Low | High | JMH benchmarks before/after, monitor carefully |
| **SonarQube quality gate too strict** | Medium | Medium | Start with relaxed gates, gradually tighten |
| **R2DBC JSONB compatibility issues** | Low | Medium | Test early, fallback plan to adjust schema |

### Medium Priority Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **JMeter tests unreliable** | Medium | Low | Proper warm-up periods, realistic data |
| **CI/CD pipeline too slow** | Medium | Low | Parallelize stages, optimize Docker layers |
| **Documentation outdated quickly** | High | Low | Automate documentation generation where possible |

***

## Open Questions

### Reactive Migration
1. **Cache Strategy**: Spring Cache reactive adapter or custom `Mono.cache()`?
    - **Recommendation**: Use `Mono.cache(Duration)` for simplicity
2. **Controller Style**: Functional endpoints or reactive `@RestController`?
    - **Recommendation**: Functional endpoints for full reactive stack
3. **Error Handling**: How to handle blocking Flyway migrations in reactive app?
    - **Answer**: Keep JDBC driver in test scope, Flyway runs before app start

### Performance Testing
4. **Performance Targets**: What are acceptable SLAs for latency/throughput?
    - **Recommendation**: Use current CQRS baseline as minimum acceptable
5. **Test Environment**: Where to run JMeter tests in CI/CD?
    - **Recommendation**: GitHub Actions with Docker PostgreSQL service

### CI/CD
6. **SonarQube Hosting**: Self-hosted or SonarCloud?
    - **Recommendation**: Local Docker for development, SonarCloud for CI/CD
7. **Coverage Target**: Is 75% realistic or should we aim lower initially?
    - **Analysis**: Current 62% is close, achievable with a few more tests
8. **Pipeline Failure**: Should performance test failures block deployment?
    - **Recommendation**: Make it a warning initially, blocking after baseline

***

## Appendices

### A. Technology Stack Updates

| Component | Before | After | Reason |
|-----------|--------|-------|--------|
| Web Framework | Spring MVC (blocking) | Spring WebFlux (reactive) | Evaluation criteria |
| Data Access | Spring Data JPA | Spring Data R2DBC | Required for reactive |
| Database Driver | PostgreSQL JDBC | R2DBC PostgreSQL | Non-blocking I/O |
| Testing (Integration) | MockMvc | WebTestClient | Reactive test support |
| Caching | Caffeine (blocking) | Reactive Mono.cache() | Reactive compatibility |
| Performance Testing | JMH (micro) | JMH + JMeter (load) | Full load testing |
| Code Quality | Manual | SonarQube | Automated quality gates |
| CI/CD | None | GitHub Actions | Automation |

### B. File Changes Summary

**New Files**:
- `.github/workflows/ci-cd.yml` - CI/CD pipeline
- `docker-compose.sonar.yml` - Local SonarQube
- `sonar-project.properties` - SonarQube config
- `src/test/jmeter/*.jmx` - JMeter test plans
- `src/test/jmeter/test_data.csv` - Test data
- `docs/CI_CD.md` - Pipeline documentation
- `docs/PERFORMANCE_TESTING.md` - JMeter guide
- `docs/ADR-001-reactive-migration.md` - Architecture decision

**Modified Files**:
- `pom.xml` - Add WebFlux, R2DBC, JaCoCo, SonarQube, JMeter plugins
- `application.yml` - R2DBC configuration
- `ProductPriceTimelineRepositoryPort.java` - Return `Mono<>`
- `PriceService.java` - Reactive implementation
- `PriceController.java` OR new `PriceHandler.java` - Reactive endpoints
- `PostgreSQLProductPriceTimelineAdapter.java` - R2DBC repository
- `PriceControllerIntegrationTest.java` - WebTestClient
- `Makefile` - Add sonar and perf targets
- `README.md` - Update setup instructions

### C. Acceptance Checklist

#### Hexagonal Architecture + DDD ✅
- [x] Clean layer separation (domain → application → infrastructure)
- [x] Domain layer has zero framework dependencies
- [x] Value objects implemented (ProductId, BrandId, Money, Priority)
- [x] Aggregate root pattern (ProductPriceTimeline)
- [x] Ports & adapters clearly defined
- [x] Repository pattern with port interfaces

#### Reactive Spring WebFlux ⚠️
- [ ] All repositories return `Mono<>` or `Flux<>`
- [ ] Controllers use `WebTestClient` or functional endpoints
- [ ] No blocking calls in hot path (verified with BlockHound)
- [ ] Reactive caching implemented
- [ ] All 5 mandatory tests passing with reactive stack

#### JMeter Performance Testing ❌
- [ ] 5 test scenarios implemented (baseline, stress, spike, endurance, cache)
- [ ] Maven plugin configured
- [ ] HTML dashboard reports generating
- [ ] Baseline metrics documented
- [ ] Test data parameterized

#### CI/CD with SonarQube ❌
- [ ] GitHub Actions workflow functional
- [ ] Unit tests execute in pipeline
- [ ] Integration tests execute with Testcontainers
- [ ] SonarQube analysis completes
- [ ] Quality gate configured and enforced
- [ ] JaCoCo coverage ≥75%
- [ ] Pipeline documentation complete

***

**Document Version**: 2.0 (Refined based on current status)  
**Last Updated**: 2026-02-11  
**Next Review**: After Phase 1 (reactive migration completion)
# ITX Backend Technical Test - Agent Guidelines

## 1. Project Overview
This project is a RESTful API implementation for an e-commerce domain, specifically handling "Prices".
The core objective is to build a service that queries the applicable price for a specific product and brand at a given timestamp, based on a tiered pricing strategy.
Agents working on this repository must ensure the final solution is production-ready, clean, and strictly adheres to the architectural constraints.

## 2. Tech Stack & Constraints
The following technologies and libraries are **mandatory**:

*   **Language:** Java 17 or 21 (LTS).
*   **Framework:** Spring Boot 3.x.
*   **Build Tool:** Maven (preferred) or Gradle.
*   **Database:** H2 (In-memory database).
*   **Component Mapping:** MapStruct (for DTO <-> Domain mapping).
*   **Boilerplate Reduction:** Lombok.
*   **Testing:** JUnit 5, Mockito.

## 3. Architecture: Hexagonal (Ports & Adapters)
Strict adherence to **Hexagonal Architecture** is required.
The goal is to isolate the domain logic entirely from infrastructure details (frameworks, databases, web).

### 3.1 Package Structure
The application must be structured into distinct layers:

1.  **Domain (Core) - `/domain`**
    *   **Responsibility:** Pure business logic.
    *   **Dependencies:** ZERO dependencies on Spring, JPA, or external frameworks. Only standard Java libraries and Lombok.
    *   **Components:**
        *   `model`: Pure Java POJOs/Records representing the business entities.
        *   `ports.inbound`: Interfaces defining Use Cases (e.g., `GetPriceUseCase`).
        *   `ports.outbound`: Interfaces defining Infrastructure needs (e.g., `PriceRepositoryPort`).
        *   `service`: Implementation of Inbound Ports (Use Cases).

2.  **Infrastructure (Adapters) - `/infrastructure`**
    *   **Responsibility:** Implement interfaces to talk to the outside world.
    *   **Dependencies:** Spring Boot, JPA, H2, REST libraries.
    *   **Components:**
        *   `adapters.rest`: REST Controllers (`@RestController`). Maps HTTP requests to Inbound Ports.
        *   `adapters.h2`: JPA Entities (`@Entity`) and Spring Data Repositories. Implements Outbound Ports.
        *   **Mapper:** Use MapStruct to convert between `Infrastructure DTOs` and `Domain Models`.

3.  **Application (Boot) - `/application`**
    *   Main class and Spring configuration.

## 4. Coding Standards & Conventions

### 4.1 Naming Conventions
*   **Classes:** Standard PascalCase (e.g., `PriceService`).
*   **Variables/Methods:** standard camelCase.
*   **Constants:** UPPER_SNAKE_CASE.
*   **Domain Objects:** Name them clearly (e.g., `Price`, `Brand`).
*   **DTOs:** Suffix with `Dto` or `Request`/`Response` (e.g., `PriceResponseDto`).
*   **Implementations:** Suffix with `Adapter` for infrastructure (e.g., `H2PriceRepositoryAdapter`).
*   **Interfaces:** Do NOT use `I` prefix.

### 4.2 Formatting & Style
*   **Indentation:** 4 spaces.
*   **Line Length:** 120 characters max.
*   **Imports:**
    *   Avoid wildcard imports (`import java.util.*;`).
    *   Order: `java.`, `javax.`, `org.`, `com.`, static imports.
*   **Annotations:** Place annotations on separate lines above the class/field/method.

### 4.3 Lombok Usage
*   Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` for DTOs and Entities.
*   Prefer `@Value` or `@Builder` (immutable) for Domain Models if possible, or careful use of `@Data`.
*   **Avoid:** `@Slf4j` in Domain layer if it introduces a dependency on a specific logging implementation (though usually SLF4J API is fine).

### 4.4 Error Handling
*   Use a Global Exception Handler (`@ControllerAdvice`).
*   Define custom Domain Exceptions (e.g., `PriceNotFoundException`).
*   Map exceptions to standard HTTP status codes:
    *   `400 Bad Request`: Invalid input/parameters.
    *   `404 Not Found`: Resource not found.
    *   `500 Internal Server Error`: Unexpected failures.

## 5. API Specifications: Get Applicable Price
*   **Method:** `GET /prices`
*   **Description:** Retrieve the single applicable price for a product/brand at a specific time.
*   **Query Parameters:**
    *   `applicationDate` (ISO-8601 LocalDateTime).
    *   `productId` (Long).
    *   `brandId` (Long).
*   **Response JSON:**
    ```json
    {
      "productId": 35455,
      "brandId": 1,
      "priceList": 1,
      "startDate": "2020-06-14T00:00:00",
      "endDate": "2020-12-31T23:59:59",
      "price": 35.50
    }
    ```

## 6. Testing Strategy
High test coverage is mandatory.

### 6.1 Unit Tests
*   **Scope:** Domain Services.
*   **Tools:** JUnit 5, Mockito.
*   **Constraint:** Do not load Spring Context (`@SpringBootTest`) for domain unit tests. They must be fast.

### 6.2 Integration Tests
*   **Scope:** End-to-End flow (Controller -> Service -> Repository -> H2).
*   **Tools:** `@SpringBootTest`, `TestRestTemplate` or `MockMvc`.
*   **Data:** Pre-load H2 with `data.sql` provided in the requirements.
*   **Mandatory Scenarios:**
    1.  Day 14th, 10:00, Product 35455, Brand 1 (ZARA).
    2.  Day 14th, 16:00, Product 35455, Brand 1 (ZARA).
    3.  Day 14th, 21:00, Product 35455, Brand 1 (ZARA).
    4.  Day 15th, 10:00, Product 35455, Brand 1 (ZARA).
    5.  Day 16th, 21:00, Product 35455, Brand 1 (ZARA).

## 7. Build & Run Instructions

### 7.1 Build
```bash
./mvnw clean install
```

### 7.2 Run Application
```bash
./mvnw spring-boot:run
```

### 7.3 Testing Commands
**Run All Tests:**
```bash
./mvnw test
```

**Run Single Test Class:**
```bash
./mvnw -Dtest=PriceControllerTest test
```

**Run Single Test Method:**
```bash
./mvnw -Dtest=PriceControllerTest#testScenario1 test
```

**Run with Debug Logging:**
```bash
./mvnw test -X
```

## 8. Agent Behavior Guidelines
*   **No Hallucinations:** Do not invent libraries that are not in `pom.xml`. If you need a library, check if it's standard or ask to add it.
*   **Atomic Changes:** Make changes in small, logical steps.
*   **Verification:** Always run tests after changing logic.
*   **File Creation:** Always verify the directory structure exists before writing files.

.PHONY: help clean compile test test-unit test-integration test-single run build package install verify start stop docker-up docker-down docker-logs check-docker

# Default target - show help
.DEFAULT_GOAL := help

# Colors for output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

##@ General

help: ## Display this help message
	@echo ""
	@echo "$(BLUE)┌─────────────────────────────────────────────────────────────┐$(NC)"
	@echo "$(BLUE)│   ITX Prices API - Development Commands                    │$(NC)"
	@echo "$(BLUE)└─────────────────────────────────────────────────────────────┘$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make $(GREEN)<target>$(NC)\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)
	@echo ""

##@ Build & Compilation

clean: ## Clean build artifacts
	@echo "$(BLUE)🧹 Cleaning build artifacts...$(NC)"
	@./mvnw clean
	@echo "$(GREEN)✓ Clean complete$(NC)"

compile: ## Compile source code
	@echo "$(BLUE)🔨 Compiling source code...$(NC)"
	@./mvnw compile
	@echo "$(GREEN)✓ Compilation complete$(NC)"

build: clean ## Clean and compile the project
	@echo "$(BLUE)🏗️  Building project...$(NC)"
	@./mvnw clean compile
	@echo "$(GREEN)✓ Build complete$(NC)"

package: ## Package the application (creates JAR)
	@echo "$(BLUE)📦 Packaging application...$(NC)"
	@./mvnw clean package -DskipTests
	@echo "$(GREEN)✓ Package created: target/prices-api-0.0.1-SNAPSHOT.jar$(NC)"

install: ## Install artifact to local Maven repository
	@echo "$(BLUE)📥 Installing to local Maven repository...$(NC)"
	@./mvnw clean install
	@echo "$(GREEN)✓ Install complete$(NC)"

##@ Testing

test: ## Run all tests (unit + integration)
	@echo "$(BLUE)🧪 Running all tests...$(NC)"
	@./mvnw test
	@echo "$(GREEN)✓ All tests passed!$(NC)"

test-unit: ## Run only unit tests (fast, no Docker)
	@echo "$(BLUE)⚡ Running unit tests...$(NC)"
	@./mvnw test -Dtest=PriceServiceTest
	@echo "$(GREEN)✓ Unit tests passed!$(NC)"

test-integration: ## Run only integration tests (requires Docker)
	@echo "$(BLUE)🐳 Running integration tests...$(NC)"
	@./mvnw test -Dtest="*IntegrationTest"
	@echo "$(GREEN)✓ Integration tests passed!$(NC)"

test-single: ## Run a single test class (usage: make test-single TEST=ClassName)
	@if [ -z "$(TEST)" ]; then \
		echo "$(RED)❌ Error: TEST variable not set$(NC)"; \
		echo "Usage: make test-single TEST=PriceServiceTest"; \
		exit 1; \
	fi
	@echo "$(BLUE)🧪 Running test: $(TEST)$(NC)"
	@./mvnw test -Dtest=$(TEST)

test-verbose: ## Run all tests with verbose output
	@echo "$(BLUE)🧪 Running all tests (verbose)...$(NC)"
	@./mvnw test -X

verify: ## Run all tests and verify package (full CI check)
	@echo "$(BLUE)✅ Running full verification...$(NC)"
	@./mvnw clean verify
	@echo "$(GREEN)✓ Verification complete!$(NC)"

##@ Application

run: ## Run the application (requires PostgreSQL on localhost:5432)
	@echo "$(BLUE)🚀 Starting application...$(NC)"
	@echo "$(YELLOW)⚠️  Note: Requires PostgreSQL running on localhost:5432$(NC)"
	@echo "$(YELLOW)   Database: pricesdb$(NC)"
	@echo "$(YELLOW)   User: postgres$(NC)"
	@echo "$(YELLOW)   Password: postgres$(NC)"
	@echo ""
	@./mvnw spring-boot:run

start: docker-up run ## Start PostgreSQL and run application

##@ Docker (PostgreSQL)

check-docker: ## Check if Docker is running
	@docker info > /dev/null 2>&1 || (echo "$(RED)❌ Docker is not running. Please start Docker Desktop.$(NC)" && exit 1)

docker-up: check-docker ## Start PostgreSQL container
	@echo "$(BLUE)🐳 Starting PostgreSQL container...$(NC)"
	@docker run --name prices-postgres \
		-e POSTGRES_DB=pricesdb \
		-e POSTGRES_USER=postgres \
		-e POSTGRES_PASSWORD=postgres \
		-p 5432:5432 \
		-d postgres:16-alpine || \
		docker start prices-postgres || true
	@echo "$(GREEN)✓ PostgreSQL is running on port 5432$(NC)"
	@echo "$(YELLOW)   Connection: jdbc:postgresql://localhost:5432/pricesdb$(NC)"
	@sleep 2

docker-down: ## Stop and remove PostgreSQL container
	@echo "$(BLUE)🛑 Stopping PostgreSQL container...$(NC)"
	@docker stop prices-postgres 2>/dev/null || true
	@docker rm prices-postgres 2>/dev/null || true
	@echo "$(GREEN)✓ PostgreSQL container stopped$(NC)"

docker-logs: ## Show PostgreSQL container logs
	@docker logs -f prices-postgres

docker-shell: ## Open psql shell in PostgreSQL container
	@echo "$(BLUE)🐚 Opening PostgreSQL shell...$(NC)"
	@echo "$(YELLOW)   Database: pricesdb$(NC)"
	@docker exec -it prices-postgres psql -U postgres -d pricesdb

##@ Development

dev: docker-up ## Start development environment (PostgreSQL + auto-reload)
	@echo "$(BLUE)💻 Starting development environment...$(NC)"
	@./mvnw spring-boot:run

watch: ## Run application with auto-reload on code changes
	@echo "$(BLUE)👀 Running with auto-reload...$(NC)"
	@./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

debug: docker-up ## Run application in debug mode (port 5005)
	@echo "$(BLUE)🐛 Starting application in debug mode...$(NC)"
	@echo "$(YELLOW)   Debug port: 5005$(NC)"
	@./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

##@ API Testing

test-api: ## Test API endpoint (requires app to be running)
	@echo "$(BLUE)🌐 Testing API endpoint...$(NC)"
	@echo ""
	@echo "$(YELLOW)Test 1: 2020-06-14 10:00 (Expected: Price List 1, 35.50 EUR)$(NC)"
	@curl -s "http://localhost:8080/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1" | jq '.' || echo "Install jq for formatted output"
	@echo ""
	@echo "$(YELLOW)Test 2: 2020-06-14 16:00 (Expected: Price List 2, 25.45 EUR)$(NC)"
	@curl -s "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1" | jq '.' || echo "Install jq for formatted output"
	@echo ""

curl-test: ## Quick API test with curl (simpler output)
	@echo "$(BLUE)📡 Quick API test...$(NC)"
	@curl "http://localhost:8080/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
	@echo ""

##@ Code Quality

format: ## Format code with Maven formatter (if configured)
	@echo "$(BLUE)✨ Formatting code...$(NC)"
	@echo "$(YELLOW)⚠️  Note: Add spring-javaformat-maven-plugin to pom.xml for auto-formatting$(NC)"

lint: ## Check code style
	@echo "$(BLUE)🔍 Checking code style...$(NC)"
	@./mvnw checkstyle:check || echo "$(YELLOW)⚠️  Add Checkstyle plugin to pom.xml$(NC)"

##@ Utilities

info: ## Show project information
	@echo ""
	@echo "$(BLUE)┌─────────────────────────────────────────────────────────────┐$(NC)"
	@echo "$(BLUE)│   Project Information                                       │$(NC)"
	@echo "$(BLUE)└─────────────────────────────────────────────────────────────┘$(NC)"
	@echo ""
	@echo "$(GREEN)Project:$(NC)      ITX Prices API"
	@echo "$(GREEN)Version:$(NC)      0.0.1-SNAPSHOT"
	@echo "$(GREEN)Java:$(NC)         21"
	@echo "$(GREEN)Spring Boot:$(NC)  3.2.2"
	@echo "$(GREEN)Database:$(NC)     PostgreSQL 16"
	@echo ""
	@echo "$(YELLOW)Endpoints:$(NC)"
	@echo "  GET /prices?applicationDate={date}&productId={id}&brandId={id}"
	@echo ""
	@echo "$(YELLOW)Test Data:$(NC)"
	@echo "  Product ID: 35455"
	@echo "  Brand ID:   1 (ZARA)"
	@echo "  Date Range: 2020-06-14 to 2020-12-31"
	@echo ""

status: ## Check application and database status
	@echo "$(BLUE)📊 Checking status...$(NC)"
	@echo ""
	@echo -n "$(YELLOW)Application:$(NC) "
	@curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null | grep -q 200 && echo "$(GREEN)✓ Running$(NC)" || echo "$(RED)✗ Not running$(NC)"
	@echo -n "$(YELLOW)PostgreSQL:$(NC)  "
	@docker ps | grep -q prices-postgres && echo "$(GREEN)✓ Running$(NC)" || echo "$(RED)✗ Not running$(NC)"
	@echo ""

clean-all: clean docker-down ## Clean everything (build + Docker)
	@echo "$(GREEN)✓ All cleaned!$(NC)"

##@ Documentation

docs: ## Open API documentation (if Swagger is configured)
	@echo "$(BLUE)📚 Opening API documentation...$(NC)"
	@echo "$(YELLOW)⚠️  Add springdoc-openapi to pom.xml for Swagger UI$(NC)"
	@echo "$(YELLOW)   Then visit: http://localhost:8080/swagger-ui.html$(NC)"

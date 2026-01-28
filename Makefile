.PHONY: help docker-up docker-down docker-logs test test-unit test-integration run clean compile benchmark

help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

docker-up: ## Start PostgreSQL via Docker Compose
	docker-compose up -d
	@echo "Waiting for PostgreSQL to be ready..."
	@sleep 5
	@echo "✅ PostgreSQL is ready at localhost:5432"

docker-down: ## Stop PostgreSQL
	docker-compose down

docker-logs: ## View PostgreSQL logs
	docker-compose logs -f postgres

test: ## Run all tests (unit + integration)
	./mvnw clean test

test-unit: ## Run unit tests only (fast, no database)
	./mvnw test -Dtest=*Test,!*IntegrationTest

test-integration: docker-up ## Run integration tests only (requires Docker)
	./mvnw test -Dtest=*IntegrationTest

run: docker-up ## Start the application (requires PostgreSQL)
	./mvnw spring-boot:run

compile: ## Compile the project without running tests
	./mvnw clean compile

package: test ## Package the application as JAR
	./mvnw package

benchmark: docker-up ## Run JMH performance benchmarks
	@echo "Running performance benchmarks..."
	./mvnw clean test-compile exec:exec@run-benchmarks

clean: ## Clean build artifacts and stop Docker containers
	./mvnw clean
	docker-compose down -v

verify: test ## Run full verification (tests + quality checks)
	./mvnw verify

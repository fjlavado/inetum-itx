.PHONY: help docker-up docker-down docker-logs test test-unit test-integration run clean compile benchmark perf-test perf-baseline perf-stress perf-spike perf-endurance perf-cache perf-report

help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

docker-up: ## Start PostgreSQL via Docker Compose
	docker compose up -d
	@echo "Waiting for PostgreSQL to be ready..."
	@sleep 5
	@echo "✅ PostgreSQL is ready at localhost:5432"

docker-down: ## Stop PostgreSQL
	docker compose down

docker-logs: ## View PostgreSQL logs
	docker compose logs -f postgres

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
	docker compose down -v

verify: test ## Run full verification (tests + quality checks)
	./mvnw verify

# JMeter Performance Testing
perf-test: docker-up ## Run all JMeter performance tests
	@echo "Running all JMeter performance tests..."
	./mvnw clean verify -DskipTests
	@echo "✅ All performance tests completed. Check target/jmeter/results/ for reports."

perf-baseline: docker-up ## Run JMeter baseline load test (100 users, 10 min)
	@echo "Running baseline load test (100 users, 10 minutes)..."
	./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_BaselineLoad.jmx
	@echo "✅ Baseline test completed. Results in target/jmeter/results/"

perf-stress: docker-up ## Run JMeter stress test (100-1000 users, 15 min)
	@echo "Running stress test (100-1000 users, 15 minutes)..."
	./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_StressTest.jmx
	@echo "✅ Stress test completed. Results in target/jmeter/results/"

perf-spike: docker-up ## Run JMeter spike test (sudden 2000 users)
	@echo "Running spike test (sudden load to 2000 users)..."
	./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_SpikeTest.jmx
	@echo "✅ Spike test completed. Results in target/jmeter/results/"

perf-endurance: docker-up ## Run JMeter endurance test (200 users, 60 min)
	@echo "Running endurance/soak test (200 users, 60 minutes)..."
	./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_EnduranceTest.jmx
	@echo "⏱️  This test takes 60 minutes. Results in target/jmeter/results/"

perf-cache: docker-up ## Run JMeter cache validation test
	@echo "Running cache validation test..."
	./mvnw clean verify -DskipTests -Djmeter.testFiles=PricingService_CacheValidation.jmx
	@echo "✅ Cache validation completed. Check metrics for hit rates."

perf-report: ## Open JMeter HTML dashboard report
	@if [ -d "target/jmeter/results" ]; then \
		echo "Opening JMeter HTML reports..."; \
		xdg-open target/jmeter/results/index.html 2>/dev/null || open target/jmeter/results/index.html 2>/dev/null || echo "⚠️  Open target/jmeter/results/index.html manually"; \
	else \
		echo "⚠️  No JMeter results found. Run 'make perf-test' first."; \
	fi

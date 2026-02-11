# CI/CD Pipeline Documentation

This document describes the continuous integration and continuous deployment (CI/CD) pipeline for the Prices API, including code quality gates, test automation, and SonarQube integration.

## Overview

The Prices API uses a comprehensive CI/CD pipeline that:

- **Builds and tests** code automatically on every push and pull request
- **Measures code coverage** with JaCoCo (target: ≥75%)
- **Analyzes code quality** with SonarQube (maintainability, reliability, security)
- **Runs performance tests** on main branch deployments
- **Packages and archives** build artifacts for deployment

## Pipeline Architecture

### GitHub Actions Workflow

**File**: `.github/workflows/ci-cd.yml`

**Trigger Events**:
- Push to `main`, `develop`, or `feature/**` branches
- Pull requests targeting `main` or `develop`

### Pipeline Jobs

```
┌─────────────────────┐
│   Build and Test    │  ← Compile, Unit Tests, Integration Tests, SonarQube
└──────────┬──────────┘
           │
           ├─────────────────────┐
           │                     │
   ┌───────▼──────────┐  ┌──────▼─────────┐
   │ Performance Tests│  │ Quality Gate   │
   │  (main only)     │  │   Enforcement  │
   └───────┬──────────┘  └────────────────┘
           │
   ┌───────▼──────────┐
   │     Package      │  ← JAR artifact creation
   │   (main only)    │
   └──────────────────┘
```

## Job 1: Build and Test

### Steps

1. **Checkout Code**
   - Fetches full git history for SonarQube blame analysis

2. **Set up JDK 21**
   - Uses Eclipse Temurin distribution
   - Caches Maven dependencies for faster builds

3. **Compile**
   ```bash
   ./mvnw clean compile -DskipTests
   ```

4. **Run Unit Tests**
   ```bash
   ./mvnw test -Dtest=*Test,!*IntegrationTest
   ```
   - Executes fast, isolated unit tests
   - No database required

5. **Run Integration Tests**
   ```bash
   ./mvnw test -Dtest=*IntegrationTest
   ```
   - Uses Testcontainers with PostgreSQL 16
   - Tests full reactive stack end-to-end

6. **Generate JaCoCo Coverage Report**
   ```bash
   ./mvnw jacoco:report
   ```
   - Produces XML and HTML coverage reports
   - Uploads to SonarQube for analysis

7. **SonarQube Analysis**
   ```bash
   ./mvnw sonar:sonar \
     -Dsonar.projectKey=com.inetum.prices:prices-api \
     -Dsonar.organization=${SONAR_ORGANIZATION} \
     -Dsonar.host.url=${SONAR_HOST_URL} \
     -Dsonar.token=${SONAR_TOKEN}
   ```
   - Analyzes code quality, security, maintainability
   - Enforces quality gate thresholds

8. **Upload Artifacts**
   - Coverage reports: `target/site/jacoco/`
   - Test results: `target/surefire-reports/`

### Environment Variables

```yaml
JAVA_VERSION: '21'
JAVA_DISTRIBUTION: 'temurin'
MAVEN_OPTS: -Xmx3072m -Dhttp.keepAlive=false
```

### Service Containers

**PostgreSQL**:
```yaml
postgres:
  image: postgres:16-alpine
  env:
    POSTGRES_DB: pricesdb_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  ports:
    - 5432:5432
  options: --health-cmd pg_isready
```

## Job 2: Performance Tests

**Trigger**: Only on `main` branch pushes

### Steps

1. **Build Application JAR**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Start Application**
   ```bash
   java -jar target/prices-api-0.0.1-SNAPSHOT.jar &
   curl --retry 10 --retry-connrefused http://localhost:8080/actuator/health
   ```

3. **Run JMeter Baseline Test**
   ```bash
   ./mvnw verify -DskipTests \
     -Djmeter.testFiles=PricingService_BaselineLoad.jmx \
     -Dduration.seconds=300
   ```
   - Runs 5-minute baseline test (reduced from 10 min for CI)
   - Continues on error (doesn't block deployment)

4. **Upload JMeter Results**
   - HTML dashboard: `target/jmeter/results/`

## Job 3: Package

**Trigger**: Only on `main` branch after successful tests

### Steps

1. **Package Application**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Upload JAR Artifact**
   - File: `target/prices-api-0.0.1-SNAPSHOT.jar`
   - Retention: 90 days (GitHub Actions default)

3. **Generate Build Info**
   ```
   Build Date: 2026-02-11
   Commit SHA: abc123...
   Branch: refs/heads/main
   Triggered by: developer-name
   ```

## Code Coverage with JaCoCo

### Configuration

**Plugin**: `jacoco-maven-plugin` 0.8.11

**Executions**:
1. `prepare-agent`: Instruments code before test execution
2. `report`: Generates coverage report after tests
3. `check`: Enforces coverage thresholds

**Thresholds**:
- **Line Coverage**: ≥ 75%
- **Branch Coverage**: ≥ 70%

**Exclusions**:
- DTOs (`application/rest/dto/**`)
- Configuration classes (`application/config/**`)
- JPA entities (`infrastructure/persistence/entity/**`)
- Converters (`infrastructure/persistence/converter/**`)
- Main application class (`PricesApplication.class`)

### Commands

```bash
# Generate coverage report
make coverage

# View local report
open target/site/jacoco/index.html

# Check coverage thresholds
./mvnw verify
```

### Coverage Reports

**XML Report**: `target/site/jacoco/jacoco.xml` (for SonarQube)
**HTML Report**: `target/site/jacoco/index.html` (for developers)

## SonarQube Integration

### Configuration

**File**: `sonar-project.properties`

**Project Metadata**:
```properties
sonar.projectKey=com.inetum.prices:prices-api
sonar.projectName=Prices API
sonar.projectVersion=0.0.1-SNAPSHOT
sonar.organization=inetum
```

**Source Paths**:
```properties
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

**Exclusions**:
- Same as JaCoCo exclusions
- Benchmark code (`**/benchmark/**`)

### Quality Gate

**Default SonarQube Quality Gate Conditions**:

| Metric                    | Threshold       | Severity |
|---------------------------|-----------------|----------|
| Coverage                  | ≥ 75%           | Error    |
| New Code Coverage         | ≥ 80%           | Error    |
| Duplicated Lines          | < 3%            | Warning  |
| Maintainability Rating    | A               | Error    |
| Reliability Rating        | A               | Error    |
| Security Rating           | A               | Error    |
| Bugs                      | 0               | Error    |
| Vulnerabilities           | 0               | Error    |
| Security Hotspots Reviewed| 100%            | Error    |

**Custom Quality Gate Configuration**:
1. Log in to SonarQube: http://localhost:9000 (or SonarCloud)
2. Navigate to **Quality Gates**
3. Create custom gate: "Prices API Gate"
4. Add conditions based on project requirements
5. Assign to project

### Commands

```bash
# Start local SonarQube
make sonar-up

# Wait for SonarQube to start (60-90 seconds)
# Access: http://localhost:9000
# Default credentials: admin / admin

# Run local analysis
make sonar-scan

# Open SonarQube dashboard
make sonar-report

# Stop SonarQube
make sonar-down
```

## Local SonarQube Setup

### Docker Compose

**File**: `docker-compose.sonar.yml`

**Services**:
1. **SonarQube** (port 9000)
   - Image: `sonarqube:10-community`
   - Volumes: data, logs, extensions
   - Depends on PostgreSQL

2. **PostgreSQL** (port 5432, internal)
   - Image: `postgres:15-alpine`
   - Database: `sonarqube`
   - User: `sonarqube` / `sonarqube`

### First-Time Setup

```bash
# 1. Start SonarQube
make sonar-up

# 2. Wait for initialization (check logs)
docker compose -f docker-compose.sonar.yml logs -f sonarqube

# 3. Access SonarQube
# URL: http://localhost:9000
# Credentials: admin / admin

# 4. Change password (prompted on first login)

# 5. Generate authentication token
# - User menu → My Account → Security → Generate Token
# - Save token securely (required for CI/CD)

# 6. Run first analysis
make sonar-scan
```

### Persistent Data

All data is stored in Docker volumes:
- `sonarqube_data`: Analysis results, projects, users
- `sonarqube_logs`: Server logs
- `sonarqube_extensions`: Plugins, customizations
- `sonarqube_db_data`: PostgreSQL database

**Backup**:
```bash
docker compose -f docker-compose.sonar.yml down
docker volume ls | grep sonarqube
# Backup volumes as needed
```

## GitHub Secrets Configuration

Required secrets for GitHub Actions:

### SonarCloud (Recommended)

1. **SONAR_TOKEN**
   - Generate in SonarCloud: Account → Security → Generate Token
   - Scope: Analyze projects
   - Expiration: None (or 90 days)

2. **SONAR_HOST_URL**
   - Value: `https://sonarcloud.io`

3. **SONAR_ORGANIZATION**
   - Your SonarCloud organization key

### Self-Hosted SonarQube (Alternative)

1. **SONAR_TOKEN**
   - Generate in SonarQube: User menu → My Account → Security

2. **SONAR_HOST_URL**
   - Value: `https://your-sonarqube-server.com`
   - Must be publicly accessible for GitHub Actions

### Adding Secrets to GitHub

1. Repository → Settings → Secrets and variables → Actions
2. Click "New repository secret"
3. Name: `SONAR_TOKEN`, Value: (paste token)
4. Repeat for `SONAR_HOST_URL` and `SONAR_ORGANIZATION`

## Running CI/CD Locally

### Full Test Suite

```bash
# 1. Start dependencies
make docker-up

# 2. Run tests with coverage
make coverage

# 3. Run SonarQube analysis
make sonar-up
make sonar-scan

# 4. View results
make sonar-report
```

### Simulating CI Environment

```bash
# Use same commands as GitHub Actions

# Compile
./mvnw clean compile -DskipTests

# Unit tests
./mvnw test -Dtest=*Test,!*IntegrationTest

# Integration tests (requires PostgreSQL)
make docker-up
./mvnw test -Dtest=*IntegrationTest

# Coverage report
./mvnw jacoco:report

# SonarQube analysis
./mvnw sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin

# Package
./mvnw package -DskipTests
```

## Troubleshooting

### Common Issues

#### 1. SonarQube Quality Gate Fails

**Symptom**: GitHub Actions build fails with "Quality gate failed"

**Causes**:
- Code coverage below 75%
- New bugs or vulnerabilities introduced
- Code smells exceeding threshold

**Solutions**:
```bash
# Check local SonarQube for details
make sonar-scan
make sonar-report

# Improve coverage
make coverage
# Add missing tests

# Fix code smells
# Review SonarQube "Issues" tab
```

#### 2. JaCoCo Coverage Check Fails

**Symptom**: Build fails at `jacoco:check` with coverage below threshold

**Solutions**:
```bash
# Generate coverage report
make coverage

# Identify uncovered code
open target/site/jacoco/index.html

# Add unit tests for uncovered lines
# Focus on domain service layer (highest impact)
```

#### 3. SonarQube Connection Timeout

**Symptom**: `sonar:sonar` fails with connection timeout

**Solutions**:
```bash
# Verify SonarQube is running
docker compose -f docker-compose.sonar.yml ps

# Check SonarQube logs
docker compose -f docker-compose.sonar.yml logs sonarqube

# Restart SonarQube
make sonar-down
make sonar-up
```

#### 4. Integration Tests Fail in CI

**Symptom**: Tests pass locally but fail in GitHub Actions

**Causes**:
- Testcontainers Docker socket issues
- PostgreSQL service not ready

**Solutions**:
```yaml
# Ensure proper health checks in workflow
services:
  postgres:
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

#### 5. Maven Dependency Download Slow

**Symptom**: Build takes too long downloading dependencies

**Solutions**:
```yaml
# Ensure Maven cache is enabled
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
```

## Performance Optimization

### GitHub Actions

1. **Enable Maven Caching**: Already configured
2. **Parallel Test Execution**: Configure in `pom.xml`
   ```xml
   <properties>
     <maven.test.parallel>classes</maven.test.parallel>
     <maven.test.threads>2</maven.test.threads>
   </properties>
   ```
3. **Skip Redundant Steps**: Use `if:` conditions for main-only jobs

### SonarQube Analysis

1. **Incremental Analysis**: Analyze only changed files (PR analysis)
2. **Exclude Test Code**: Already configured
3. **Disable Unused Rules**: Configure in SonarQube UI

## Best Practices

1. **Branch Protection Rules**
   - Require status checks to pass before merging
   - Require pull request reviews
   - Include SonarQube quality gate check

2. **Code Review Workflow**
   - PR opens → CI runs → SonarQube analyzes
   - Review both code and SonarQube findings
   - Address quality issues before merge

3. **Test Coverage Strategy**
   - Target 75% overall, 80% for new code
   - Prioritize domain logic coverage
   - Use mutation testing for critical paths

4. **Quality Gate Philosophy**
   - Fail fast on critical issues (bugs, vulnerabilities)
   - Warn on code smells (address in follow-up)
   - Track technical debt over time

5. **Performance Testing in CI**
   - Run full suite on main branch only
   - Use shorter durations (5 min vs 10 min)
   - Continue on error (informational)

## Metrics and Monitoring

### Key Metrics to Track

1. **Build Success Rate**: Target > 95%
2. **Average Build Time**: Baseline < 10 minutes
3. **Code Coverage Trend**: Target ↑ over time
4. **Technical Debt Ratio**: SonarQube metric
5. **Quality Gate Pass Rate**: Target 100%

### Dashboard

Access metrics in:
- **GitHub Actions**: Repository → Actions tab
- **SonarQube**: Project → Measures
- **JaCoCo**: Local HTML reports

## Future Enhancements

### Phase 4 Improvements

1. **Docker Image Build**
   - Add Dockerfile for containerization
   - Publish to GitHub Container Registry
   - Tag with version and commit SHA

2. **Automated Deployment**
   - Deploy to staging on main branch merge
   - Deploy to production on tag push
   - Use GitHub Environments for approvals

3. **Security Scanning**
   - Dependabot for dependency updates
   - CodeQL for security vulnerabilities
   - OWASP Dependency Check

4. **Notification**s
   - Slack notifications on build failure
   - Email on quality gate failure
   - GitHub commit status checks

5. **Release Automation**
   - Semantic versioning
   - Automated changelog generation
   - GitHub Releases with artifacts

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [SonarQube Documentation](https://docs.sonarqube.org/latest/)
- [SonarCloud](https://sonarcloud.io/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

---

**Last Updated**: 2026-02-11
**Phase**: Phase 3 - CI/CD Pipeline with SonarQube
**Status**: Fully implemented and ready for deployment

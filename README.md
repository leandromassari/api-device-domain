# Device Management API

A production-ready Spring Boot 3.2+ REST API for device domain management, built with Java 21 and following hexagonal architecture (Ports & Adapters) principles.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Code Quality & Security](#code-quality--security)
- [Docker Deployment](#docker-deployment)
- [API Documentation](#api-documentation)
- [Health Checks](#health-checks)
- [Project Structure](#project-structure)

## Overview

This API provides comprehensive device management capabilities including:

- Create, read, update, and delete device records
- Device state management (AVAILABLE, IN_USE, INACTIVE)
- Query devices by brand and state
- State transition validation
- Business rule enforcement (e.g., cannot delete devices in use)

**Key Features:**
- Hexagonal architecture with clear separation of concerns
- Framework-agnostic domain layer
- Comprehensive test coverage (80%+ with JaCoCo)
- Mutation testing with PITest
- Security vulnerability scanning with OWASP Dependency Check
- OpenAPI/Swagger documentation
- Docker containerization with multi-stage builds
- Database migrations with Flyway
- Production-ready observability (Spring Actuator)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│              (HTTP Controllers & DTOs)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 Application Layer                            │
│              (Use Cases / Services)                          │
│  CreateDevice │ GetDevice │ UpdateDevice │ DeleteDevice     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Domain Layer                               │
│         (Business Logic & Entities)                          │
│  Device Model │ DeviceState │ Business Rules                │
│                DeviceRepositoryPort                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               Persistence Layer                              │
│         (Repository Adapter & JPA)                           │
│  DeviceRepositoryAdapter │ JPA Repository │ PostgreSQL      │
└─────────────────────────────────────────────────────────────┘
```

**See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed architecture documentation.**

## Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9+
- **Docker**: For containerized deployment (optional)
- **PostgreSQL**: 16+ (or use Docker)
- **Git**: For version control

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd api-device-domain
```

### 2. Configure Environment Variables

Create a `.env` file in the project root or set environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/devicedb` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASS` | Database password | `postgres` |
| `DB_MAX_CONNECTIONS` | Connection pool size | `10` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | (none) |
| `NVD_API_KEY` | OWASP NVD API key (optional) | (none) |

### 3. Setup PostgreSQL Database

**Option A: Using Docker**
```bash
docker run --name device-postgres \
  -e POSTGRES_DB=devicedb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16-alpine
```

**Option B: Local PostgreSQL**
```sql
CREATE DATABASE devicedb;
```

### 4. Build the Project

```bash
# Full build with tests
mvn clean install

# Compile only (skip tests)
mvn clean compile -DskipTests
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/device-domain`

### 6. Verify Installation

```bash
# Check health endpoint
curl http://localhost:8080/device-domain/actuator/health

# Access Swagger UI
open http://localhost:8080/device-domain/swagger-ui.html
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

Integration tests use Testcontainers to spin up a PostgreSQL container automatically.

### Generate Coverage Report

```bash
mvn jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

**Coverage Requirements:**
- Minimum line coverage: 80%
- Minimum branch coverage: 80%

### Run Mutation Tests

```bash
mvn pitest:mutationCoverage
```

Mutation test report will be generated at: `target/pit-reports/index.html`

### Run All Quality Checks

```bash
mvn clean verify jacoco:report pitest:mutationCoverage
```

## Code Quality & Security

### Format Code (Google Java Style)

```bash
# Format all code
mvn fmt:format

# Check formatting without changes
mvn fmt:check
```

### Security Vulnerability Scan

```bash
mvn dependency-check:check
```

Report will be generated at: `target/dependency-check-report.html`

**Note:** Set `NVD_API_KEY` environment variable to avoid rate limiting.

## Docker Deployment

### Build Docker Image

```bash
docker build -t device-api:latest .
```

### Run with Docker Compose

```bash
# Start all services (API + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f device-api

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Docker Compose Services

- **postgres**: PostgreSQL 16 database on port 5432
- **device-api**: Spring Boot application on port 8080

### Environment Variables for Docker

Configure in `docker-compose.yml` or create a `.env` file:

```env
DB_URL=jdbc:postgresql://postgres:5432/devicedb
DB_USER=postgres
DB_PASS=postgres
DB_MAX_CONNECTIONS=10
SPRING_PROFILES_ACTIVE=prod
```

## API Documentation

### Swagger UI

Interactive API documentation with request/response examples:

```
http://localhost:8080/device-domain/swagger-ui.html
```

### OpenAPI Specification

Raw OpenAPI JSON:

```
http://localhost:8080/device-domain/v3/api-docs
```

### Quick API Overview

**Base URL:** `/device-domain/api/v1/devices`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/devices` | Create new device |
| GET | `/api/v1/devices` | List all devices |
| GET | `/api/v1/devices/{id}` | Get device by ID |
| GET | `/api/v1/devices/brand/{brand}` | Get devices by brand |
| GET | `/api/v1/devices/state/{state}` | Get devices by state |
| PUT | `/api/v1/devices/{id}` | Full update device |
| PATCH | `/api/v1/devices/{id}` | Partial update device |
| DELETE | `/api/v1/devices/{id}` | Delete device |

**See [docs/API.md](docs/API.md) for detailed endpoint documentation with examples.**

## Health Checks

### Application Health

```bash
curl http://localhost:8080/device-domain/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Exposed Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information

**Note:** Health details are shown only when authorized.

### Docker Health Checks

Docker containers include built-in health checks:

```bash
# Check container health
docker ps

# View health check logs
docker inspect --format='{{json .State.Health}}' device-api | jq
```

## Project Structure

```
api-device-domain/
├── src/
│   ├── main/
│   │   ├── java/com/project/device/
│   │   │   ├── domain/                    # Domain layer (framework-agnostic)
│   │   │   │   ├── model/                 # Entities and value objects
│   │   │   │   │   ├── Device.java
│   │   │   │   │   └── DeviceState.java
│   │   │   │   ├── port/                  # Output ports (interfaces)
│   │   │   │   │   └── DeviceRepositoryPort.java
│   │   │   │   └── exception/             # Domain exceptions
│   │   │   │       ├── DeviceNotFoundException.java
│   │   │   │       ├── DeviceInUseException.java
│   │   │   │       └── InvalidDeviceOperationException.java
│   │   │   ├── application/               # Application layer
│   │   │   │   └── usecase/               # Use case implementations
│   │   │   │       ├── CreateDeviceUseCase.java
│   │   │   │       ├── GetDeviceUseCase.java
│   │   │   │       ├── UpdateDeviceUseCase.java
│   │   │   │       └── DeleteDeviceUseCase.java
│   │   │   ├── infrastructure/            # Infrastructure layer
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── persistence/       # Database adapter
│   │   │   │   │   │   ├── DeviceEntity.java
│   │   │   │   │   │   ├── DeviceJpaRepository.java
│   │   │   │   │   │   ├── DeviceRepositoryAdapter.java
│   │   │   │   │   │   └── DeviceMapper.java
│   │   │   │   │   └── rest/              # REST adapter
│   │   │   │   │       ├── DeviceController.java
│   │   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │   │       ├── DeviceMapper.java
│   │   │   │   │       └── dto/
│   │   │   │   │           ├── DeviceRequest.java
│   │   │   │   │           ├── DeviceResponse.java
│   │   │   │   │           ├── DeviceUpdateRequest.java
│   │   │   │   │           └── ErrorResponse.java
│   │   │   │   └── config/                # Spring configuration
│   │   │   │       └── OpenApiConfig.java
│   │   │   └── DeviceApplication.java     # Spring Boot entry point
│   │   └── resources/
│   │       ├── application.yaml           # Main configuration
│   │       └── db/migration/              # Flyway migrations
│   │           ├── V1__Create_Device_Table.sql
│   │           └── V2__Add_Brand_State_Indexes.sql
│   └── test/
│       ├── java/com/project/device/       # Test files mirror main structure
│       │   ├── domain/model/
│       │   ├── application/usecase/
│       │   └── infrastructure/adapter/
│       └── resources/
│           └── application-test.yaml      # Test configuration
├── docs/                                   # Documentation
│   ├── ARCHITECTURE.md
│   ├── API.md
│   └── FUTURE_IMPROVEMENTS.md
├── Dockerfile                              # Multi-stage Docker build
├── docker-compose.yml                      # Docker orchestration
├── pom.xml                                 # Maven configuration
├── CONTRIBUTING.md                         # Development guidelines
└── README.md                               # This file
```

### Layer Responsibilities

**Domain Layer** (`domain/`)
- Pure business logic
- No framework dependencies
- Contains entities, value objects, and business rules
- Defines output ports (interfaces)

**Application Layer** (`application/`)
- Orchestrates use cases
- Implements business workflows
- Depends only on domain layer
- Transaction boundaries

**Infrastructure Layer** (`infrastructure/`)
- External concerns (database, REST, config)
- Implements domain ports
- Framework-specific code (Spring, JPA)
- Input/output adapters

## Development Commands

### Maven Commands

```bash
mvn clean install                   # Full build with tests
mvn clean compile                   # Compile only
mvn test                            # Run tests
mvn verify                          # Run integration tests
mvn jacoco:report                   # Coverage report
mvn pitest:mutationCoverage         # Mutation tests
mvn dependency-check:check          # Security scan
mvn fmt:format                      # Format code
mvn spring-boot:run                 # Run application
```

## Database Migrations

Database schema is managed by Flyway migrations in `src/main/resources/db/migration/`:

- `V1__Create_Device_Table.sql` - Initial schema
- `V2__Add_Brand_State_Indexes.sql` - Performance indexes

Migrations run automatically on application startup.

### Manual Migration Commands

```bash
# Migration info
mvn flyway:info

# Run migrations
mvn flyway:migrate

# Clean database (development only!)
mvn flyway:clean
```

## Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs device-postgres

# Verify connection
psql -h localhost -U postgres -d devicedb
```

### Port Conflicts

If port 8080 is in use, change it in `application.yaml`:

```yaml
server:
  port: 8081
```

### Test Failures

```bash
# Clean and rebuild
mvn clean install

# Skip tests temporarily
mvn clean install -DskipTests
```

### Docker Issues

```bash
# Clean Docker resources
docker-compose down -v
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines, coding standards, and how to contribute to this project.

## License

This project is licensed under the [MIT License](LICENSE).

## Support & Contact

For issues, questions, or contributions, please:
- Open an issue on GitHub
- Contact me (leandromassari@gmail.com)
- Check the documentation in `docs/`

---

Built with Java 21, Spring Boot 3.2, and Hexagonal Architecture principles.

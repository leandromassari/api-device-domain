# Architecture Documentation

## Table of Contents

- [Overview](#overview)
- [Hexagonal Architecture](#hexagonal-architecture)
- [Layer Structure](#layer-structure)
- [Design Patterns](#design-patterns)
- [Dependency Flow](#dependency-flow)
- [Technology Decisions](#technology-decisions)
- [Data Flow](#data-flow)
- [Extension Points](#extension-points)

## Overview

The Device Management API is built using **Hexagonal Architecture** (also known as Ports and Adapters pattern), which promotes separation of concerns, testability, and maintainability by isolating the business logic from external dependencies.

### Core Principles

1. **Domain Independence**: Business logic has no dependencies on frameworks or infrastructure
2. **Dependency Inversion**: All dependencies point inward toward the domain
3. **Testability**: Each layer can be tested in isolation
4. **Flexibility**: Easy to swap implementations of external services
5. **Maintainability**: Clear boundaries between concerns

## Hexagonal Architecture

### Conceptual Model

```
         ┌─────────────────────────────────────────┐
         │     External World (Adapters)           │
         │  ┌─────────────┐    ┌──────────────┐   │
         │  │ REST API    │    │  Database    │   │
         │  │  (Input)    │    │  (Output)    │   │
         │  └──────┬──────┘    └──────▲───────┘   │
         │         │                   │           │
         └─────────┼───────────────────┼───────────┘
                   │                   │
         ┌─────────▼───────────────────┴───────────┐
         │                                          │
         │       Application Layer (Use Cases)     │
         │                                          │
         │  ┌────────────────────────────────────┐ │
         │  │  CreateDevice  │  GetDevice        │ │
         │  │  UpdateDevice  │  DeleteDevice     │ │
         │  └────────────────────────────────────┘ │
         │                   │                      │
         └───────────────────┼──────────────────────┘
                             │
         ┌───────────────────▼──────────────────────┐
         │                                           │
         │           Domain Layer (Core)            │
         │                                           │
         │  ┌──────────────────────────────────┐    │
         │  │  Business Logic & Rules          │    │
         │  │  - Device Model                  │    │
         │  │  - DeviceState                   │    │
         │  │  - Validation Rules              │    │
         │  │  - State Transitions             │    │
         │  │                                  │    │
         │  │  Ports (Interfaces)              │    │
         │  │  - DeviceRepositoryPort          │    │
         │  └──────────────────────────────────┘    │
         │                                           │
         └───────────────────────────────────────────┘
```

### Key Components

**Ports (Interfaces)**
- Define contracts between layers
- Input Ports: Use case interfaces (implicit in our case - use case classes)
- Output Ports: Repository interfaces that domain needs (`DeviceRepositoryPort`)

**Adapters (Implementations)**
- Input Adapters: Translate external requests into domain operations (REST controllers)
- Output Adapters: Implement domain ports to interact with external systems (Repository adapters, JPA)

**Domain (Core)**
- Contains business logic and rules
- Completely isolated from infrastructure concerns
- No Spring, JPA, or HTTP dependencies

## Layer Structure

### 1. Domain Layer

**Location**: `src/main/java/com/project/device/domain/`

**Responsibilities**:
- Define business entities and value objects
- Implement business rules and validation
- Declare output ports (interfaces for external dependencies)
- Throw domain-specific exceptions

**Components**:

#### Models (`domain/model/`)
```java
Device.java
├── id: UUID
├── name: String
├── brand: String
├── state: DeviceState
├── creationTime: LocalDateTime
└── Business Methods:
    ├── canDelete(): boolean
    ├── canUpdateNameOrBrand(): boolean
    └── validateStateTransition(DeviceState): void

DeviceState.java (Enum)
├── AVAILABLE
├── IN_USE
└── INACTIVE
```

#### Ports (`domain/port/`)
```java
DeviceRepositoryPort.java (Interface)
├── save(Device): Device
├── findById(UUID): Optional<Device>
├── findAll(): List<Device>
├── findByBrand(String): List<Device>
├── findByState(DeviceState): List<Device>
├── deleteById(UUID): void
└── existsById(UUID): boolean
```

#### Exceptions (`domain/exception/`)
- `DeviceNotFoundException` - 404 scenarios
- `DeviceInUseException` - 409 conflict scenarios
- `InvalidDeviceOperationException` - 400 business rule violations

**Design Rules**:
- No external framework dependencies
- Pure Java business logic
- All domain models are immutable or carefully controlled
- State transitions are validated at domain level

---

### 2. Application Layer

**Location**: `src/main/java/com/project/device/application/usecase/`

**Responsibilities**:
- Orchestrate business workflows (use cases)
- Coordinate between domain and adapters
- Define transaction boundaries
- Implement business processes

**Components**:

#### Use Cases
```java
CreateDeviceUseCase.java
├── Validates input (name, brand, state)
├── Generates UUID
├── Sets creation timestamp
├── Saves via DeviceRepositoryPort
└── Returns created Device

GetDeviceUseCase.java
├── findById(UUID)
├── findAll()
├── findByBrand(String)
└── findByState(DeviceState)

UpdateDeviceUseCase.java
├── fullUpdate() - replaces all fields
├── partialUpdate() - updates only provided fields
├── Validates business rules (IN_USE restrictions)
└── Validates state transitions

DeleteDeviceUseCase.java
├── Checks device exists
├── Validates can delete (not IN_USE)
└── Deletes via repository
```

**Design Rules**:
- Annotated with `@Service` (Spring component)
- Depends only on domain layer (models, ports, exceptions)
- No knowledge of HTTP, databases, or frameworks
- Contains business workflow logic, not business rules

---

### 3. Infrastructure Layer

**Location**: `src/main/java/com/project/device/infrastructure/`

**Responsibilities**:
- Implement adapters for external systems
- REST API exposure
- Database persistence
- Framework configuration

#### 3.1 Persistence Adapter (`infrastructure/adapter/persistence/`)

```java
DeviceEntity.java (JPA Entity)
├── Maps to "devices" table
├── JPA annotations (@Entity, @Table, @Column)
└── Mirrors domain model structure

DeviceJpaRepository.java (Spring Data JPA)
├── extends JpaRepository<DeviceEntity, UUID>
├── Custom query methods:
    ├── findByBrand(String)
    └── findByState(DeviceState)

DeviceRepositoryAdapter.java
├── @Component
├── Implements DeviceRepositoryPort (domain interface)
├── Translates between Device ↔ DeviceEntity
├── Delegates to DeviceJpaRepository
└── Hides JPA complexity from domain

DeviceMapper.java (Persistence)
├── toEntity(Device): DeviceEntity
└── toDomain(DeviceEntity): Device
```

**Flow**: Use Case → DeviceRepositoryPort → DeviceRepositoryAdapter → JpaRepository → PostgreSQL

#### 3.2 REST Adapter (`infrastructure/adapter/rest/`)

```java
DeviceController.java
├── @RestController
├── Endpoint mappings:
    ├── POST /api/v1/devices
    ├── GET /api/v1/devices
    ├── GET /api/v1/devices/{id}
    ├── GET /api/v1/devices/brand/{brand}
    ├── GET /api/v1/devices/state/{state}
    ├── PUT /api/v1/devices/{id}
    ├── PATCH /api/v1/devices/{id}
    └── DELETE /api/v1/devices/{id}
├── Translates HTTP requests to use case calls
├── Translates domain models to DTOs
└── Returns appropriate HTTP status codes

GlobalExceptionHandler.java
├── @ControllerAdvice
├── Translates domain exceptions to HTTP responses:
    ├── DeviceNotFoundException → 404
    ├── DeviceInUseException → 409
    ├── InvalidDeviceOperationException → 400
    ├── IllegalArgumentException → 400
    ├── MethodArgumentNotValidException → 400
    └── Generic Exception → 500

DeviceMapper.java (REST)
└── toResponse(Device): DeviceResponse

DTOs:
├── DeviceRequest (create/full update)
├── DeviceUpdateRequest (partial update)
├── DeviceResponse (read operations)
└── ErrorResponse (error handling)
```

**Flow**: HTTP Request → Controller → Use Case → Domain → Controller → HTTP Response

#### 3.3 Configuration (`infrastructure/config/`)

```java
OpenApiConfig.java
├── Configures Swagger UI
├── API documentation metadata
└── OpenAPI v3 specification
```

---

## Design Patterns

### 1. Hexagonal Architecture (Ports & Adapters)

**Purpose**: Isolate business logic from infrastructure

**Implementation**:
- **Port**: `DeviceRepositoryPort` interface in domain
- **Adapter**: `DeviceRepositoryAdapter` in infrastructure implements port
- **Benefit**: Can swap database implementation without changing domain

### 2. Use Case Pattern

**Purpose**: Encapsulate business workflows

**Implementation**:
- Each business operation is a separate use case class
- `CreateDeviceUseCase`, `GetDeviceUseCase`, etc.
- **Benefit**: Clear, testable business operations

### 3. Repository Pattern

**Purpose**: Abstract data access

**Implementation**:
- `DeviceRepositoryPort` defines contract
- `DeviceRepositoryAdapter` implements using JPA
- **Benefit**: Domain doesn't know about SQL or JPA

### 4. Mapper Pattern

**Purpose**: Translate between layers

**Implementation**:
- `DeviceMapper` (persistence): Domain ↔ Entity
- `DeviceMapper` (REST): Domain ↔ DTO
- **Benefit**: Clear separation of models per layer

### 5. Dependency Injection

**Purpose**: Loose coupling and testability

**Implementation**:
- Spring's constructor injection
- Use cases depend on `DeviceRepositoryPort` interface
- **Benefit**: Easy to mock dependencies in tests

### 6. Exception Translation

**Purpose**: Map internal errors to appropriate external responses

**Implementation**:
- Domain throws specific exceptions
- `GlobalExceptionHandler` translates to HTTP status codes
- **Benefit**: Clean separation of error handling

### 7. DTO Pattern

**Purpose**: Decouple API contracts from domain models

**Implementation**:
- Request DTOs: `DeviceRequest`, `DeviceUpdateRequest`
- Response DTOs: `DeviceResponse`, `ErrorResponse`
- **Benefit**: API can evolve independently of domain

### 8. State Pattern (Implicit)

**Purpose**: Manage device state transitions

**Implementation**:
- `DeviceState` enum with validation
- `Device.validateStateTransition()` enforces rules
- **Benefit**: Centralized state transition logic

---

## Dependency Flow

### Dependency Rule

**Dependencies point INWARD toward the domain.**

```
┌─────────────────────────────────────────┐
│  Infrastructure Layer (Adapters)        │  ← Framework specific
│  - REST Controllers                     │
│  - JPA Repositories                     │
│  - Configuration                        │
└──────────────┬──────────────────────────┘
               │ depends on
               ▼
┌─────────────────────────────────────────┐
│  Application Layer (Use Cases)          │  ← Business workflows
│  - CreateDeviceUseCase                  │
│  - GetDeviceUseCase                     │
│  - UpdateDeviceUseCase                  │
│  - DeleteDeviceUseCase                  │
└──────────────┬──────────────────────────┘
               │ depends on
               ▼
┌─────────────────────────────────────────┐
│  Domain Layer (Core)                    │  ← Pure business logic
│  - Device, DeviceState                  │
│  - DeviceRepositoryPort                 │
│  - Domain Exceptions                    │
└─────────────────────────────────────────┘
     ▲
     │ implemented by
     │
Infrastructure Adapters
```

### Example: Create Device Flow

```
1. HTTP POST /api/v1/devices (DeviceRequest)
   ↓
2. DeviceController.createDevice()
   │ - Validates input (@Valid annotation)
   │ - Calls use case
   ↓
3. CreateDeviceUseCase.execute(name, brand, state)
   │ - Generates UUID
   │ - Creates Device domain model
   │ - Calls repository port
   ↓
4. DeviceRepositoryPort.save(device)
   ↓
5. DeviceRepositoryAdapter.save(device)
   │ - Maps Device → DeviceEntity
   │ - Calls JPA repository
   ↓
6. DeviceJpaRepository.save(entity)
   │ - JPA/Hibernate
   ↓
7. PostgreSQL database
   ↓
8. Response flows back up:
   Device → DeviceResponse → HTTP 201 Created
```

---

## Technology Decisions

### 1. Java 21

**Reasons**:
- Latest LTS with modern language features
- Virtual threads for improved concurrency (future enhancement)
- Pattern matching and records for cleaner code
- Performance improvements

### 2. Spring Boot 3.2

**Reasons**:
- Production-ready framework with minimal configuration
- Excellent ecosystem (Spring Data, Actuator, Security)
- Native image support with GraalVM (future option)
- Active development and community support

### 3. PostgreSQL

**Reasons**:
- ACID compliance for data integrity
- Excellent performance with proper indexing
- Rich SQL feature set
- Open-source and widely adopted

### 4. Flyway

**Reasons**:
- Version-controlled database migrations
- Repeatable and reliable deployments
- Supports rollbacks (with proper planning)
- Integration with Maven and Spring Boot

### 5. Hexagonal Architecture

**Reasons**:
- Clear separation of concerns
- Testability - domain can be tested without Spring/database
- Flexibility - easy to swap implementations
- Maintainability - changes are localized

### 6. JPA/Hibernate

**Reasons**:
- Standardized ORM specification
- Reduces boilerplate SQL
- Object-relational mapping
- Change tracking and caching

**Trade-offs**:
- Additional abstraction layer
- Potential N+1 query issues (mitigated with proper fetching)
- Complexity for simple CRUD operations

### 7. OpenAPI/Swagger

**Reasons**:
- Automatic API documentation
- Interactive testing interface
- Client code generation support
- Industry standard for REST APIs

### 8. Docker

**Reasons**:
- Consistent environments (dev, test, prod)
- Easy deployment and scaling
- Dependency isolation
- Multi-stage builds for optimized images

---

## Data Flow

### Read Operation (GET Device by ID)

```
HTTP GET /api/v1/devices/{id}
    ↓
DeviceController.getDeviceById(id)
    ↓
GetDeviceUseCase.findById(id)
    ↓
DeviceRepositoryPort.findById(id)
    ↓
DeviceRepositoryAdapter.findById(id)
    ↓
DeviceJpaRepository.findById(id)
    ↓
PostgreSQL SELECT query
    ↓
Optional<DeviceEntity>
    ↓
DeviceMapper.toDomain(entity) → Optional<Device>
    ↓
If empty: throw DeviceNotFoundException (404)
If present: Device
    ↓
DeviceMapper.toResponse(device) → DeviceResponse
    ↓
HTTP 200 OK + JSON response
```

### Write Operation (Update Device)

```
HTTP PUT /api/v1/devices/{id} + DeviceRequest
    ↓
DeviceController.fullUpdateDevice(id, request)
    ↓
UpdateDeviceUseCase.fullUpdate(id, name, brand, state)
    ├─ findById(id) or throw 404
    ├─ Validate canUpdateNameOrBrand() → 400 if IN_USE
    ├─ Validate state transition → 400 if invalid
    ├─ Create updated Device (preserves creationTime)
    └─ save(device)
    ↓
DeviceRepositoryAdapter.save(device)
    ├─ DeviceMapper.toEntity(device)
    └─ jpaRepository.save(entity)
    ↓
PostgreSQL UPDATE query
    ↓
DeviceEntity (updated)
    ↓
DeviceMapper.toDomain(entity) → Device
    ↓
DeviceMapper.toResponse(device) → DeviceResponse
    ↓
HTTP 200 OK + JSON response
```

### Delete Operation

```
HTTP DELETE /api/v1/devices/{id}
    ↓
DeviceController.deleteDevice(id)
    ↓
DeleteDeviceUseCase.execute(id)
    ├─ findById(id) or throw 404
    ├─ device.canDelete() → false if IN_USE
    │   └─ throw DeviceInUseException (409)
    └─ deleteById(id)
    ↓
DeviceRepositoryAdapter.deleteById(id)
    ↓
DeviceJpaRepository.deleteById(id)
    ↓
PostgreSQL DELETE query
    ↓
HTTP 204 No Content
```

---

## Extension Points

### Adding New Features

The architecture provides clear extension points for common enhancements:

#### 1. New Domain Behavior

**Location**: `domain/model/Device.java`

Add new business rules or validation logic:
```java
public boolean canBeLent() {
    return state == DeviceState.AVAILABLE && isOperational;
}
```

#### 2. New Use Case

**Location**: `application/usecase/`

Create new use case class:
```java
@Service
public class LendDeviceUseCase {
    private final DeviceRepositoryPort repository;
    // Implementation
}
```

#### 3. New Query Method

**Step 1**: Add to port (`domain/port/DeviceRepositoryPort.java`):
```java
List<Device> findAvailableDevicesByBrand(String brand);
```

**Step 2**: Implement in adapter (`infrastructure/adapter/persistence/`):
```java
@Override
public List<Device> findAvailableDevicesByBrand(String brand) {
    return jpaRepository.findByBrandAndState(brand, DeviceState.AVAILABLE)
        .stream()
        .map(DeviceMapper::toDomain)
        .toList();
}
```

**Step 3**: Add to JPA repository:
```java
List<DeviceEntity> findByBrandAndState(String brand, DeviceState state);
```

#### 4. New REST Endpoint

**Location**: `infrastructure/adapter/rest/DeviceController.java`

```java
@GetMapping("/available/brand/{brand}")
public ResponseEntity<List<DeviceResponse>> getAvailableByBrand(
    @PathVariable String brand
) {
    List<Device> devices = getDeviceUseCase.findAvailableDevicesByBrand(brand);
    return ResponseEntity.ok(devices.stream()
        .map(DeviceMapper::toResponse)
        .toList());
}
```

#### 5. New Domain Exception

**Step 1**: Create exception (`domain/exception/`):
```java
public class DeviceLimitExceededException extends RuntimeException {
    public DeviceLimitExceededException(String message) {
        super(message);
    }
}
```

**Step 2**: Handle in controller advice:
```java
@ExceptionHandler(DeviceLimitExceededException.class)
public ResponseEntity<ErrorResponse> handleDeviceLimitExceeded(
    DeviceLimitExceededException ex
) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.from(403, ex.getMessage()));
}
```

#### 6. Alternative Persistence Implementation

To swap to a different database (e.g., MongoDB):

**Step 1**: Create new adapter:
```java
@Component
public class DeviceMongoAdapter implements DeviceRepositoryPort {
    private final MongoTemplate mongoTemplate;
    // Implement all methods
}
```

**Step 2**: Conditional configuration:
```java
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "mongo")
public class MongoConfig {
    @Bean
    public DeviceRepositoryPort deviceRepository() {
        return new DeviceMongoAdapter();
    }
}
```

**Domain and application layers remain unchanged!**

#### 7. New Input Adapter (e.g., GraphQL)

**Location**: `infrastructure/adapter/graphql/`

```java
@Controller
public class DeviceGraphQLController {
    private final CreateDeviceUseCase createDeviceUseCase;

    @QueryMapping
    public DeviceResponse device(@Argument UUID id) {
        Device device = getDeviceUseCase.findById(id);
        return DeviceMapper.toResponse(device);
    }
}
```

**Use cases remain unchanged!**

---

## Testing Strategy

### Domain Layer Tests

**No mocking required** - pure business logic:
```java
@Test
void deviceInUseShouldNotBeDeleted() {
    Device device = new Device(UUID.randomUUID(), "iPhone", "Apple",
                               DeviceState.IN_USE, LocalDateTime.now());
    assertFalse(device.canDelete());
}
```

### Application Layer Tests

**Mock repository port**:
```java
@Test
void createDeviceShouldGenerateIdAndTimestamp() {
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    Device device = useCase.execute("iPhone", "Apple", DeviceState.AVAILABLE);

    assertNotNull(device.id());
    assertNotNull(device.creationTime());
}
```

### Infrastructure Layer Tests

**Integration tests with Testcontainers**:
```java
@SpringBootTest
@Testcontainers
class DeviceControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void createDevice_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/v1/devices")
            .contentType(MediaType.APPLICATION_JSON)
            .content(deviceRequestJson))
            .andExpect(status().isCreated());
    }
}
```

---

## Architecture Benefits

### 1. Testability

Each layer can be tested independently:
- Domain: Pure unit tests
- Application: Mock repository
- Infrastructure: Integration tests

### 2. Flexibility

Easy to:
- Swap database implementations
- Add new input channels (GraphQL, gRPC)
- Change frameworks (Spring → Micronaut)

### 3. Maintainability

Clear boundaries:
- Business rules in domain
- Workflows in application
- Technical details in infrastructure

### 4. Team Scalability

Teams can work independently:
- Backend team: Domain + Application
- API team: REST adapters
- Data team: Persistence adapters

### 5. Evolution

Architecture supports:
- Adding features without breaking existing code
- Refactoring with confidence
- Incremental improvements

---

## Architecture Constraints

### What to Avoid

1. **Domain depending on infrastructure**: Never import Spring, JPA, or HTTP in domain
2. **Use cases doing infrastructure work**: No SQL or HTTP in use cases
3. **Crossing boundaries**: Don't expose entities or DTOs across boundaries
4. **Anemic domain models**: Domain should contain business logic, not just data

### Enforcing Constraints

**Tools**:
- ArchUnit (for automated architecture tests)
- Code reviews
- Package visibility rules
- CI/CD checks

---

## Conclusion

This hexagonal architecture provides a solid foundation for the Device Management API:

- **Clean separation** between business logic and infrastructure
- **High testability** with clear boundaries
- **Flexibility** to evolve and swap implementations
- **Maintainability** through organized code structure

The architecture supports the current requirements while remaining flexible for future enhancements like caching, security, event sourcing, and distributed tracing.

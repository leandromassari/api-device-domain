# Contributing to Device Management API

Thank you for your interest in contributing to the Device Management API! This document provides guidelines and best practices for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Architecture Guidelines](#architecture-guidelines)
- [Code Review Checklist](#code-review-checklist)
- [Documentation](#documentation)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. Please be respectful, considerate, and professional in all interactions.

### Expected Behavior

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Acknowledge contributions
- Be patient and understanding

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Personal or political attacks
- Publishing others' private information

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- Java 21 JDK installed
- Maven 3.9+ installed
- Docker and Docker Compose (for local development)
- Git configured with your name and email
- IDE with Java support (IntelliJ IDEA, Eclipse, VS Code)

### Setting Up Development Environment

1. **Fork the repository**
   ```bash
   # Click "Fork" on GitHub, then clone your fork
   git clone https://github.com/YOUR_USERNAME/api-device-domain.git
   cd api-device-domain
   ```

2. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/api-device-domain.git
   git fetch upstream
   ```

3. **Start local PostgreSQL**
   ```bash
   docker-compose up -d postgres
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Verify setup**
   ```bash
   curl http://localhost:8080/device-domain/actuator/health
   ```

### IDE Configuration

#### IntelliJ IDEA

1. **Import project**: File → Open → Select `pom.xml`
2. **Enable annotation processing**: Settings → Build → Compiler → Annotation Processors
3. **Install plugins**: Lombok, SonarLint, Google Java Format
4. **Code style**: Import `.editorconfig` or Google Java Style

#### VS Code

1. **Install extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
   - SonarLint
   - Google Java Format

2. **Configure settings**:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.format.settings.url": "https://google.github.io/styleguide/eclipse-java-google-style.xml"
   }
   ```

## Development Workflow

### Branch Strategy

We use **Git Flow** for branch management:

- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes
- `release/*` - Release preparation

### Creating a Feature Branch

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/add-device-search

# Make changes and commit
git add .
git commit -m "feat: add device search functionality"

# Push to your fork
git push origin feature/add-device-search
```

### Keeping Branch Up to Date

```bash
# Fetch upstream changes
git fetch upstream

# Rebase your branch
git rebase upstream/main

# If conflicts occur, resolve them and continue
git add .
git rebase --continue

# Force push to your fork (after rebase)
git push origin feature/add-device-search --force-with-lease
```

## Coding Standards

### Java Style Guide

We follow **Google Java Style Guide** with these key points:

#### 1. Formatting

```java
// Good: Proper indentation and spacing
public class DeviceService {
    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    public Device create(String name, String brand) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        return repository.save(new Device(name, brand));
    }
}
```

**Formatting Rules**:
- Indent with 2 spaces (not tabs)
- Column limit: 100 characters
- No wildcard imports
- One top-level class per file
- Order: static imports, then regular imports, alphabetically

#### 2. Naming Conventions

```java
// Classes: PascalCase
public class DeviceController { }

// Methods and variables: camelCase
public Device findById(UUID deviceId) { }
private String deviceName;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_DEVICES = 1000;

// Packages: lowercase
package com.project.device.domain.model;
```

#### 3. Code Organization

**Class Structure** (top to bottom):
1. Static fields
2. Instance fields
3. Constructors
4. Public methods
5. Package-private methods
6. Private methods
7. Nested classes

```java
public class Device {
    // Static fields
    private static final Logger log = LoggerFactory.getLogger(Device.class);

    // Instance fields
    private final UUID id;
    private final String name;
    private final DeviceState state;

    // Constructor
    public Device(UUID id, String name, DeviceState state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    // Public methods
    public boolean canDelete() {
        return state != DeviceState.IN_USE;
    }

    // Private methods
    private void validate() {
        // validation logic
    }
}
```

#### 4. Use Records for Immutable Data

```java
// Good: Use records for DTOs
public record DeviceResponse(
    UUID id,
    String name,
    String brand,
    DeviceState state,
    LocalDateTime creationTime
) {}

// Avoid: Verbose class with getters/setters for simple DTOs
```

#### 5. Prefer Composition Over Inheritance

```java
// Good: Composition
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceValidator validator;

    // Constructor injection
}

// Avoid: Deep inheritance hierarchies
```

### Architecture Adherence

**Critical Rules** (enforced by code review):

1. **Domain Layer**:
   - No Spring annotations
   - No JPA annotations
   - No HTTP/REST dependencies
   - Pure Java business logic

```java
// Good: Pure domain model
public class Device {
    private final UUID id;
    private final DeviceState state;

    public boolean canDelete() {
        return state != DeviceState.IN_USE;
    }
}

// Bad: Domain with Spring annotations
@Entity  // ❌ JPA in domain
public class Device {
    @Id  // ❌ Persistence in domain
    private UUID id;
}
```

2. **Application Layer**:
   - Use cases annotated with `@Service`
   - Depend only on domain (models, ports, exceptions)
   - No knowledge of HTTP, databases, or frameworks

```java
// Good: Use case with clean dependencies
@Service
public class CreateDeviceUseCase {
    private final DeviceRepositoryPort repository;

    public Device execute(String name, String brand, DeviceState state) {
        // Business workflow logic
    }
}
```

3. **Infrastructure Layer**:
   - Implements domain ports
   - Contains all framework code
   - Adapters for REST, JPA, config

```java
// Good: Infrastructure adapter
@Component
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {
    private final DeviceJpaRepository jpaRepository;

    @Override
    public Device save(Device device) {
        DeviceEntity entity = DeviceMapper.toEntity(device);
        DeviceEntity saved = jpaRepository.save(entity);
        return DeviceMapper.toDomain(saved);
    }
}
```

### Dependency Injection

Always use **constructor injection**:

```java
// Good: Constructor injection
@Service
public class CreateDeviceUseCase {
    private final DeviceRepositoryPort repository;

    public CreateDeviceUseCase(DeviceRepositoryPort repository) {
        this.repository = repository;
    }
}

// Avoid: Field injection
@Service
public class CreateDeviceUseCase {
    @Autowired  // ❌ Avoid field injection
    private DeviceRepositoryPort repository;
}
```

**Benefits**:
- Immutable dependencies
- Easier to test (no reflection needed)
- Explicit dependencies
- Compile-time checking

## Testing Guidelines

### Test Coverage Requirements

- **Minimum**: 80% line and branch coverage
- **Target**: 90%+ coverage
- **Mutation testing**: PITest must pass

### Test Structure

Use **AAA pattern** (Arrange, Act, Assert):

```java
@Test
@DisplayName("Should throw exception when deleting device in use")
void deleteDevice_WhenInUse_ThrowsException() {
    // Arrange
    UUID deviceId = UUID.randomUUID();
    Device device = new Device(deviceId, "iPhone", "Apple",
                               DeviceState.IN_USE, LocalDateTime.now());
    when(repository.findById(deviceId)).thenReturn(Optional.of(device));

    // Act & Assert
    assertThrows(DeviceInUseException.class, () ->
        deleteDeviceUseCase.execute(deviceId)
    );
}
```

### Test Organization

Use `@Nested` for logical grouping:

```java
@DisplayName("CreateDeviceUseCase Tests")
class CreateDeviceUseCaseTest {

    @Nested
    @DisplayName("Successful Creation")
    class SuccessfulCreationTests {

        @Test
        @DisplayName("Should create device with valid inputs")
        void createDevice_WithValidInputs_Success() {
            // Test implementation
        }

        @Test
        @DisplayName("Should generate UUID automatically")
        void createDevice_ShouldGenerateId() {
            // Test implementation
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when name is blank")
        void createDevice_BlankName_ThrowsException() {
            // Test implementation
        }
    }
}
```

### Test Types

#### 1. Unit Tests (Domain & Application Layers)

```java
// Domain test - no mocking needed
@Test
void device_InUse_CannotBeDeleted() {
    Device device = new Device(UUID.randomUUID(), "iPhone", "Apple",
                               DeviceState.IN_USE, LocalDateTime.now());
    assertFalse(device.canDelete());
}

// Application test - mock repository
@Test
void createDevice_ShouldSaveToRepository() {
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    Device device = createDeviceUseCase.execute("iPhone", "Apple", DeviceState.AVAILABLE);

    verify(repository).save(any(Device.class));
    assertNotNull(device.id());
}
```

#### 2. Integration Tests (Infrastructure Layer)

```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class DeviceControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceJpaRepository repository;

    @BeforeEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/devices should create device")
    void createDevice_Success() throws Exception {
        String requestBody = """
            {
              "name": "iPhone 14",
              "brand": "Apple",
              "state": "AVAILABLE"
            }
            """;

        mockMvc.perform(post("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("iPhone 14"));
    }
}
```

### Testing Best Practices

1. **Test one thing per test**
2. **Use descriptive test names** (`@DisplayName`)
3. **Avoid test interdependencies**
4. **Clean up test data** (`@BeforeEach`, `@AfterEach`)
5. **Use test fixtures for common setup**
6. **Mock external dependencies, not domain logic**
7. **Test edge cases and error paths**
8. **Maintain test performance** (fast feedback)

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=CreateDeviceUseCaseTest

# Specific test method
mvn test -Dtest=CreateDeviceUseCaseTest#createDevice_WithValidInputs_Success

# Integration tests only
mvn verify -DskipUnitTests

# With coverage report
mvn clean verify jacoco:report

# Mutation testing
mvn pitest:mutationCoverage
```

## Commit Message Guidelines

We follow **Conventional Commits** specification.

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring (no behavior change)
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `build`: Build system or dependency changes
- `ci`: CI/CD configuration changes
- `chore`: Other changes (maintenance)

### Examples

```bash
# Feature
feat(device): add search by name functionality

Implements case-insensitive search for device names.
Adds new endpoint GET /api/v1/devices/search?name=query

Closes #42

# Bug fix
fix(delete): prevent deletion of devices in use

Changed delete logic to check device state before deletion.
Now returns 409 Conflict when attempting to delete IN_USE devices.

Fixes #56

# Documentation
docs(api): update API documentation with examples

Added cURL examples for all endpoints.
Updated error response format documentation.

# Refactoring
refactor(mapper): simplify device mapping logic

Extracted common mapping logic into utility methods.
No behavior changes.

# Test
test(usecase): add missing edge case tests

Added tests for null inputs and boundary conditions.
Coverage increased from 85% to 92%.
```

### Commit Message Rules

1. **Type is mandatory**
2. **Scope is optional** but recommended
3. **Subject is mandatory**:
   - Use imperative mood ("add" not "added" or "adds")
   - No period at the end
   - Max 50 characters
4. **Body is optional** but recommended for complex changes
5. **Footer is optional**: Reference issues (Closes #123, Fixes #456)

## Pull Request Process

### Before Creating PR

1. **Ensure all tests pass**:
   ```bash
   mvn clean verify
   ```

2. **Check code coverage**:
   ```bash
   mvn jacoco:report
   # Open target/site/jacoco/index.html
   ```

3. **Run code formatter**:
   ```bash
   mvn fmt:format
   ```

4. **Run security scan**:
   ```bash
   mvn dependency-check:check
   ```

5. **Update documentation** if needed

### Creating Pull Request

1. **Push your branch** to your fork:
   ```bash
   git push origin feature/add-device-search
   ```

2. **Open PR on GitHub**:
   - Click "Compare & pull request"
   - Choose base: `main` (or `develop`)
   - Choose compare: your feature branch

3. **Fill out PR template**:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issue
Closes #123

## Changes Made
- Added device search endpoint
- Implemented search use case
- Added integration tests
- Updated API documentation

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed
- [ ] All tests pass locally

## Checklist
- [ ] Code follows project style guidelines
- [ ] Tests provide adequate coverage (80%+)
- [ ] Documentation updated
- [ ] No new warnings or errors
- [ ] Commit messages follow convention
```

### PR Review Process

1. **Automated checks** must pass:
   - Build successful
   - Tests pass
   - Code coverage meets threshold
   - No security vulnerabilities

2. **Code review** by at least one maintainer

3. **Address feedback**:
   - Make requested changes
   - Push new commits to same branch
   - Respond to review comments

4. **Approval & merge**:
   - Squash commits (if needed)
   - Merge to target branch
   - Delete feature branch

## Architecture Guidelines

### Hexagonal Architecture Rules

**What Goes Where**:

#### Domain Layer (`domain/`)
✅ **Allowed**:
- Business entities and value objects
- Business rules and validation
- Domain exceptions
- Port interfaces (contracts)

❌ **Not Allowed**:
- Spring annotations (except basic @Component if needed)
- JPA/Hibernate annotations
- HTTP/REST dependencies
- Database-specific code

#### Application Layer (`application/`)
✅ **Allowed**:
- Use case implementations
- Business workflow orchestration
- Transaction boundaries
- Spring `@Service` annotation

❌ **Not Allowed**:
- HTTP request/response handling
- Database queries (use ports)
- Framework-specific code (except Spring DI)

#### Infrastructure Layer (`infrastructure/`)
✅ **Allowed**:
- REST controllers and DTOs
- JPA entities and repositories
- Spring configuration
- All framework-specific code

❌ **Not Allowed**:
- Business logic
- Business rules

### Adding New Features

Follow this checklist when adding features:

1. **Domain Layer**:
   - [ ] Add/update domain model
   - [ ] Implement business rules
   - [ ] Add domain exceptions if needed
   - [ ] Update repository port interface

2. **Application Layer**:
   - [ ] Create/update use case
   - [ ] Add validation logic
   - [ ] Handle exceptions

3. **Infrastructure Layer**:
   - [ ] Implement repository adapter
   - [ ] Add REST endpoint
   - [ ] Create request/response DTOs
   - [ ] Add exception handler

4. **Database**:
   - [ ] Create Flyway migration (if schema changes)
   - [ ] Update JPA entity

5. **Tests**:
   - [ ] Domain model tests
   - [ ] Use case tests (with mocks)
   - [ ] Integration tests (with Testcontainers)

6. **Documentation**:
   - [ ] Update API.md
   - [ ] Update README if needed
   - [ ] Add JavaDoc for public APIs

## Code Review Checklist

### For Authors

Before requesting review:
- [ ] Code follows style guidelines
- [ ] Tests added and passing
- [ ] Documentation updated
- [ ] No commented-out code
- [ ] No debug statements
- [ ] Commits are clean and logical
- [ ] Branch is up to date with main

### For Reviewers

Check for:
- [ ] **Correctness**: Does the code do what it's supposed to?
- [ ] **Architecture**: Follows hexagonal architecture?
- [ ] **Readability**: Is code clear and well-organized?
- [ ] **Tests**: Adequate test coverage?
- [ ] **Performance**: Any obvious performance issues?
- [ ] **Security**: Any security concerns?
- [ ] **Edge cases**: Are edge cases handled?
- [ ] **Error handling**: Proper exception handling?
- [ ] **Documentation**: Is documentation clear?
- [ ] **Breaking changes**: Are breaking changes documented?

### Review Comments

Be constructive:

✅ **Good**:
```
Consider extracting this logic into a separate method for better readability:

```java
private boolean isValidTransition(DeviceState from, DeviceState to) {
    // transition logic
}
```

This would make the code more testable and easier to maintain.
```

❌ **Avoid**:
```
This code is bad. Rewrite it.
```

## Documentation

### JavaDoc Guidelines

Add JavaDoc for:
- Public classes
- Public methods
- Complex private methods

**Example**:
```java
/**
 * Creates a new device with the specified attributes.
 *
 * <p>This method validates inputs, generates a UUID, sets the creation
 * timestamp, and persists the device to the repository.
 *
 * @param name the device name, must not be blank
 * @param brand the device brand/manufacturer, must not be blank
 * @param state the initial device state, must not be null
 * @return the created device with generated ID and timestamp
 * @throws IllegalArgumentException if any parameter is invalid
 */
public Device execute(String name, String brand, DeviceState state) {
    // Implementation
}
```

### README Updates

Update README.md when:
- Adding new prerequisites
- Changing build/run commands
- Adding new environment variables
- Changing project structure

### API Documentation

Update `docs/API.md` when:
- Adding new endpoints
- Changing request/response formats
- Adding new error codes
- Changing behavior

## Getting Help

### Resources

- **Architecture**: See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **API Reference**: See [docs/API.md](docs/API.md)
- **Roadmap**: See [docs/FUTURE_IMPROVEMENTS.md](docs/FUTURE_IMPROVEMENTS.md)

### Communication

- **Questions**: Open a GitHub Discussion
- **Bug Reports**: Open an issue with "bug" label
- **Feature Requests**: Open an issue with "enhancement" label
- **Security Issues**: Email security@example.com (do not open public issue)

### Issue Templates

When opening issues, use the templates:

**Bug Report**:
```markdown
## Description
Clear description of the bug

## Steps to Reproduce
1. Step 1
2. Step 2
3. ...

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- OS: Windows 11
- Java: 21
- Maven: 3.9.4
```

**Feature Request**:
```markdown
## Feature Description
What feature do you want?

## Use Case
Why is this feature needed?

## Proposed Solution
How should it work?

## Alternatives Considered
Other approaches you've thought about
```

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- GitHub contributors page
- Release notes

Thank you for contributing! 🚀

---

**Last Updated**: 2025-10-24
**Maintained By**: Leandro Massari

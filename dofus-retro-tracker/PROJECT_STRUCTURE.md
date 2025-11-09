# Dofus Retro Price Tracker - Project Structure

**Version**: 0.1.0-SNAPSHOT
**Created**: 2025-11-09
**Status**: Wave 0 Complete

---

## Directory Structure

```
dofus-retro-tracker/
│
├── .github/                                # GitHub configuration
│   └── workflows/
│       └── ci.yml                         # CI/CD pipeline (build, test, coverage)
│
├── src/
│   ├── main/
│   │   ├── java/com/dofusretro/pricetracker/
│   │   │   ├── DofusRetroApplication.java        # Main Spring Boot application
│   │   │   │
│   │   │   ├── config/                           # Spring configuration classes
│   │   │   │   └── package-info.java             # - Database configuration
│   │   │   │                                     # - Cache configuration
│   │   │   │                                     # - Security configuration
│   │   │   │                                     # - Web MVC configuration
│   │   │   │
│   │   │   ├── model/                            # JPA entities
│   │   │   │   └── package-info.java             # - Item
│   │   │   │                                     # - ItemCategory
│   │   │   │                                     # - PriceSnapshot
│   │   │   │                                     # - MarketListing
│   │   │   │                                     # - Server
│   │   │   │                                     # - Character
│   │   │   │
│   │   │   ├── repository/                       # Spring Data JPA repositories
│   │   │   │   └── package-info.java             # - ItemRepository
│   │   │   │                                     # - PriceSnapshotRepository
│   │   │   │                                     # - MarketListingRepository
│   │   │   │
│   │   │   ├── service/                          # Business logic layer
│   │   │   │   └── package-info.java             # - ItemService
│   │   │   │                                     # - PriceAnalysisService
│   │   │   │                                     # - MarketDataService
│   │   │   │                                     # - PacketCaptureService
│   │   │   │                                     # - GuiAutomationService
│   │   │   │
│   │   │   ├── controller/                       # REST API controllers
│   │   │   │   └── package-info.java             # - ItemController
│   │   │   │                                     # - PriceController
│   │   │   │                                     # - MarketController
│   │   │   │                                     # - ServerController
│   │   │   │
│   │   │   ├── dto/                              # Data Transfer Objects
│   │   │   │   └── package-info.java             # - ItemDTO
│   │   │   │                                     # - PriceSnapshotDTO
│   │   │   │                                     # - MarketListingDTO
│   │   │   │                                     # - ErrorResponseDTO
│   │   │   │
│   │   │   ├── protocol/                         # Packet parsing & network
│   │   │   │   └── package-info.java             # - PacketCapture (pcap4j)
│   │   │   │                                     # - PacketParser
│   │   │   │                                     # - BinaryReader
│   │   │   │                                     # - MarketDataExtractor
│   │   │   │
│   │   │   ├── automation/                       # GUI automation (SikuliX)
│   │   │   │   └── package-info.java             # - AutomationEngine
│   │   │   │                                     # - ScreenCapture
│   │   │   │                                     # - ImageMatcher
│   │   │   │                                     # - MarketNavigator
│   │   │   │
│   │   │   └── exception/                        # Custom exceptions
│   │   │       └── package-info.java             # - PriceTrackerException
│   │   │                                         # - ItemNotFoundException
│   │   │                                         # - PacketCaptureException
│   │   │                                         # - GlobalExceptionHandler
│   │   │
│   │   └── resources/
│   │       ├── application.yml                   # Base configuration
│   │       ├── application-dev.yml               # Development profile
│   │       ├── application-prod.yml              # Production profile
│   │       ├── logback-spring.xml                # Logging configuration
│   │       └── db/migration/                     # Flyway migrations (Wave 1+)
│   │
│   └── test/
│       ├── java/com/dofusretro/pricetracker/
│       │   ├── DofusRetroApplicationTests.java   # Application context tests
│       │   ├── config/                           # Config tests
│       │   ├── model/                            # Entity tests
│       │   ├── repository/                       # Repository tests
│       │   ├── service/                          # Service tests
│       │   ├── controller/                       # Controller tests
│       │   ├── protocol/                         # Protocol tests
│       │   └── automation/                       # Automation tests
│       │
│       └── resources/
│           └── application-test.yml              # Test configuration (H2)
│
├── logs/                                   # Application logs (gitignored)
│   ├── dofus-retro-tracker.log           # Main log file
│   └── dofus-retro-tracker-error.log     # Error log file
│
├── .dockerignore                          # Docker build exclusions
├── .env                                   # Environment variables (dev)
├── .gitignore                             # Git exclusions
├── docker-compose.yml                     # PostgreSQL + pgAdmin services
├── Dockerfile                             # Multi-stage container build
├── pom.xml                                # Maven configuration
├── README.md                              # Project documentation
├── PROJECT_STRUCTURE.md                   # This file
└── WAVE_0_COMPLETION_REPORT.md           # Wave 0 completion report
```

---

## Key Files Description

### Build & Configuration

| File | Purpose |
|------|---------|
| `pom.xml` | Maven project configuration, dependencies, plugins |
| `docker-compose.yml` | PostgreSQL 16 + pgAdmin containers |
| `Dockerfile` | Application containerization |
| `.env` | Environment variables for development |

### Application Configuration

| File | Purpose |
|------|---------|
| `application.yml` | Base configuration (all profiles) |
| `application-dev.yml` | Development settings (debug, local DB) |
| `application-prod.yml` | Production settings (env vars, optimized) |
| `application-test.yml` | Test settings (H2 in-memory) |
| `logback-spring.xml` | Logging configuration (console + file) |

### Source Code

| Package | Purpose | Wave |
|---------|---------|------|
| `config/` | Spring configuration beans | 1 |
| `model/` | JPA entities (Item, Price, etc.) | 1 |
| `repository/` | Spring Data repositories | 1 |
| `service/` | Business logic | 2-4 |
| `controller/` | REST API endpoints | 3 |
| `dto/` | Data transfer objects | 2-3 |
| `protocol/` | Packet capture & parsing | 2 |
| `automation/` | GUI automation (SikuliX) | 4 |
| `exception/` | Custom exceptions & handlers | 2 |

### CI/CD

| File | Purpose |
|------|---------|
| `.github/workflows/ci.yml` | GitHub Actions pipeline |
| - Build job | Maven build + tests |
| - Code quality job | Maven verify |
| - Docker job | Container build |
| - Security job | Dependency scan |

---

## Technology Mapping

### By Layer

```
┌─────────────────────────────────────────────────────────┐
│                     REST API Layer                      │
│  (Spring MVC, Jackson, @RestController)                 │
│  → controller/, dto/                                    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Business Logic Layer                  │
│  (Spring @Service, @Transactional)                      │
│  → service/                                             │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Data Access Layer                     │
│  (Spring Data JPA, Hibernate)                           │
│  → repository/, model/                                  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Database                           │
│  (PostgreSQL 16)                                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│               External Data Sources                     │
├─────────────────────────────────────────────────────────┤
│  Packet Capture    →  protocol/  →  pcap4j            │
│  GUI Automation    →  automation/ →  SikuliX, JavaCV   │
└─────────────────────────────────────────────────────────┘
```

---

## Dependencies by Category

### Core Framework
- Spring Boot 3.3.5
- Spring Web MVC
- Spring Data JPA
- Spring Boot Actuator

### Database
- PostgreSQL JDBC Driver
- Flyway (migrations)
- HikariCP (connection pool)
- H2 (testing)

### Data Capture
- pcap4j 1.8.2 (packet capture)
- SikuliX 2.0.5 (GUI automation)
- JavaCV 1.5.9 (image processing)

### Utilities
- Lombok (boilerplate reduction)
- Guava 33.0.0 (utilities)
- Caffeine (caching)
- Jackson (JSON)

### Testing
- JUnit 5
- Mockito
- AssertJ
- Spring Boot Test

### Build & Quality
- Maven
- JaCoCo (code coverage)
- Maven Surefire (testing)

---

## Port Allocation

| Service | Port | Usage |
|---------|------|-------|
| Spring Boot Application | 8080 | Main application |
| PostgreSQL | 5432 | Database |
| pgAdmin (optional) | 5050 | Database management UI |
| Actuator | 8080/actuator | Health, metrics, prometheus |

---

## Environment Profiles

### Development (`dev`)
- Local PostgreSQL
- Debug logging
- SQL statement logging
- Fast startup
- Hot reload

### Production (`prod`)
- External PostgreSQL (env vars)
- Minimal logging
- Optimized performance
- Security hardened

### Test (`test`)
- H2 in-memory database
- Fast execution
- Isolated environment
- DDL auto-create

---

## Logging Strategy

### Log Files
- **Main**: `logs/dofus-retro-tracker.log` (30 days, 3GB)
- **Errors**: `logs/dofus-retro-tracker-error.log` (90 days, 1GB)

### Log Levels by Profile

| Component | Dev | Prod |
|-----------|-----|------|
| Application | DEBUG | INFO |
| Spring | DEBUG | WARN |
| Hibernate SQL | DEBUG | WARN |
| SQL Parameters | TRACE | - |

---

## Caching Strategy

- **Implementation**: Caffeine
- **TTL**: 3600s (configurable)
- **Max Size**: 10,000 entries (configurable)
- **Use Cases**:
  - Item metadata
  - Server information
  - Category lookup
  - Frequent queries

---

## Database Migration Strategy

- **Tool**: Flyway
- **Location**: `src/main/resources/db/migration/`
- **Naming**: `V{version}__{description}.sql`
- **Validation**: On startup
- **Baseline**: Enabled for existing databases

---

## Testing Strategy

### Unit Tests
- Service layer logic
- DTO conversions
- Utility functions

### Integration Tests
- Repository layer (with test containers)
- API endpoints (MockMvc)
- Database migrations

### Coverage Goals
- Minimum: 50% (enforced by JaCoCo)
- Target: 70%+

---

## Security Considerations

### Development
- ⚠️ Default credentials in `.env`
- ⚠️ SQL logging enabled
- ⚠️ Detailed error messages

### Production
- ✅ Environment variable configuration
- ✅ Minimal error disclosure
- ✅ Non-root container user
- ✅ Connection pool limits
- ✅ Actuator endpoint restrictions

---

## Performance Tuning

### JPA
- Batch inserts: 20 rows
- Order inserts/updates
- Connection pooling (HikariCP)

### JVM (Docker)
- Memory: 256MB-512MB
- GC: G1GC
- Max pause: 200ms

### Database
- Connection pool: 5-20 connections
- Statement timeout: 30s
- Idle timeout: 10min

---

## Monitoring & Observability

### Available Endpoints
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

### Metrics Tracked
- JVM metrics (memory, threads, GC)
- HTTP request metrics
- Database connection pool metrics
- Custom application metrics

---

## Development Workflow

### Local Setup
1. Start database: `docker-compose up -d`
2. Run application: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
3. Make changes (hot reload enabled)
4. Run tests: `mvn test`

### Pre-commit Checklist
- [ ] Tests pass: `mvn test`
- [ ] Build succeeds: `mvn clean install`
- [ ] Code coverage acceptable: `mvn jacoco:report`
- [ ] No compilation warnings

### Deployment
1. Build: `mvn clean package`
2. Create Docker image: `docker build -t dofus-retro-tracker .`
3. Run: `docker run -p 8080:8080 dofus-retro-tracker`

---

## Next Steps (Waves 1-4)

### Wave 1: Database Implementation
- Flyway migration scripts
- Entity classes
- Repository interfaces
- Basic CRUD operations

### Wave 2: Packet Capture
- pcap4j integration
- Protocol parser
- Data extraction
- Service layer

### Wave 3: REST API
- Controller implementations
- DTO definitions
- API documentation (OpenAPI)
- Integration tests

### Wave 4: GUI Automation
- SikuliX setup
- Image recognition
- Automation sequences
- Market navigation

---

**Last Updated**: 2025-11-09
**Status**: ✅ Wave 0 Complete
**Ready For**: Wave 1 - Database Implementation

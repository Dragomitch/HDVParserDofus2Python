# CLAUDE.md - Project Context & Development Guide

## Project Overview

**Dofus Retro Price Tracker** is a complete Java/Spring Boot reimplementation of a Python-based auction house (HDV) price tracking system, migrated from Dofus 2 to Dofus Retro.

### Migration Context

This project represents a comprehensive technology migration:

**From (Python - Dofus 2):**
- Python 3.8 with Flask
- SQLite database with SQLAlchemy ORM
- Scapy for packet capture
- PyAutoGUI for GUI automation
- React frontend for visualization

**To (Java - Dofus Retro):**
- Java 21 LTS with Spring Boot 3.3.5
- PostgreSQL 16 with JPA/Hibernate
- Pcap4j for packet capture
- SikuliX + JavaCV for GUI automation
- Angular 20 frontend with Material Design

---

## Technology Stack

### Backend (Spring Boot)

**Core Framework:**
- **Java 21** - Latest LTS release with modern language features
- **Spring Boot 3.3.5** - Enterprise application framework
- **Spring Data JPA** - Data access with Hibernate ORM
- **Maven 3.9+** - Dependency management and build tool

**Database:**
- **PostgreSQL 16** - Production database (ACID compliant)
- **Flyway** - Database migration version control
- **HikariCP** - High-performance connection pooling
- **H2** - In-memory database for testing

**Network & Automation:**
- **Pcap4j 1.8.2** - Pure Java packet capture library (cross-platform)
- **SikuliX 2.0.5** - GUI automation with image recognition
- **JavaCV 1.5.9** - Java wrapper for OpenCV (template matching)

**Utilities:**
- **Lombok** - Reduce boilerplate (@Data, @Builder, @Slf4j)
- **Caffeine** - High-performance in-memory caching
- **Guava 33.0.0** - Google core libraries

**API & Documentation:**
- **SpringDoc OpenAPI 3.0** - Swagger/OpenAPI documentation
- **Bucket4j 8.7.0** - Rate limiting
- **MapStruct 1.5.5** - DTO mapping (ready for annotation processor)

**Testing:**
- **JUnit 5** - Modern unit testing framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **@DataJpaTest, @WebMvcTest** - Spring Boot test slices

**DevOps:**
- **Docker & Docker Compose** - Containerization
- **GitHub Actions** - CI/CD pipeline
- **Spring Boot Actuator** - Monitoring and health checks
- **JaCoCo** - Code coverage reporting

### Frontend (Angular 20)

**Core Framework:**
- **Angular 20.3.9** - Standalone components, signals
- **TypeScript 5.x** - Strict mode enabled
- **RxJS 7.x** - Reactive programming

**UI & Visualization:**
- **Angular Material 20** - Material Design components
- **Chart.js 4.x** - Time-series price charts
- **ng2-charts** - Angular wrapper for Chart.js

**Testing:**
- **Jasmine 5.x** - Unit testing framework
- **Karma** - Test runner
- **Playwright** - E2E testing (optional)

**Build Tools:**
- **Angular CLI 20** - Project scaffolding and build
- **ESBuild** - Fast builds via Angular CLI

---

## System Architecture

### Multi-Module Design

The system consists of **4 core modules** working together:

#### 1. **Packet Capture Module**
- Sniffs TCP packets on port 5555 (Dofus game server)
- Uses Pcap4j with BPF filters
- Thread-safe BlockingQueue for packet buffering
- Graceful shutdown and health monitoring

#### 2. **Protocol Parser Module** (CRITICAL PATH)
- Parses Dofus Retro binary protocol
- VarInt/VarLong decoding (7-bit encoding)
- Message type identification and deserialization
- Support for compressed packets (zlib)

#### 3. **GUI Automation Module**
- State machine orchestration (5 action types)
- Template matching with OpenCV
- Platform detection (Windows/macOS/Linux)
- HiDPI/Retina display support
- Coordinate transformation

#### 4. **Business Logic & API Module**
- Service layer with caching and transactions
- Circuit breaker pattern for fault tolerance
- Background scheduled tasks
- RESTful API with OpenAPI documentation
- Global exception handling

### Data Flow

```
[Dofus Client] ←→ [Game Server]
       ↓ (sniffed)
[PacketCaptureService] → [BlockingQueue] → [PacketConsumerService]
                                                    ↓
                                          [ProtocolParser]
                                                    ↓
                                            [ItemPriceService]
                                                    ↓
                         [Cache Layer] ← [JPA Repositories] → [PostgreSQL]
                                                    ↓
                                            [REST Controllers]
                                                    ↓
                                          [Angular Frontend]
                                                    ↓
                                               [User Browser]
```

### Database Schema

**Tables:**
- **sub_categories** - Item category hierarchy (weapon, armor, consumable, etc.)
- **items** - Master item catalog (item_gid, item_name, sub_category_id)
- **price_entries** - Historical price records (price, quantity, created_at)

**Key Relationships:**
- SubCategory 1:N Item
- Item 1:N PriceEntry

**Indexes:**
- `idx_item_gid` on items(item_gid)
- `idx_created_at` on price_entries(created_at)
- `idx_item_quantity` on price_entries(item_id, quantity)

---

## Project Structure

```
HDVParserDofus2Python/
├── dofus-retro-tracker/              # Main Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dofusretro/pricetracker/
│   │   │   │   ├── model/           # JPA entities (Item, PriceEntry, SubCategory)
│   │   │   │   ├── repository/      # Spring Data repositories
│   │   │   │   ├── service/         # Business logic services
│   │   │   │   ├── controller/      # REST API controllers
│   │   │   │   ├── config/          # Spring configuration classes
│   │   │   │   ├── protocol/        # Dofus protocol parser
│   │   │   │   ├── automation/      # GUI automation framework
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── mapper/          # DTO mappers
│   │   │   │   ├── exception/       # Custom exceptions
│   │   │   │   └── task/            # Scheduled background tasks
│   │   │   └── resources/
│   │   │       ├── application.yml           # Base configuration
│   │   │       ├── application-dev.yml       # Development config
│   │   │       ├── application-prod.yml      # Production config
│   │   │       └── db/migration/             # Flyway migrations
│   │   └── test/                    # Unit & integration tests
│   ├── frontend/                    # Angular 20 application
│   │   ├── src/
│   │   │   ├── app/
│   │   │   │   ├── components/      # Angular components
│   │   │   │   ├── services/        # API services
│   │   │   │   ├── models/          # TypeScript interfaces
│   │   │   │   └── interceptors/    # HTTP interceptors
│   │   │   └── environments/        # Environment configs
│   │   └── package.json
│   ├── pom.xml                      # Maven dependencies
│   ├── Dockerfile                   # Multi-stage production build
│   └── docker-compose.yml           # PostgreSQL + app services
├── docs/                            # Documentation
│   ├── architecture.md              # System architecture
│   ├── setup.md                     # Development setup
│   └── PROTOCOL_ANALYSIS.md         # Dofus protocol specs
├── DOFUS_RETRO_PRD.md              # Product Requirements Document
├── IMPLEMENTATION_BOOK.md           # Multi-agent task breakdown
├── AGENT_ROSTER_REFINED.md         # Agent profiles and waves
├── POSSIBLE_IMPROVEMENTS.md         # Technical debt and optimizations
├── INFRASTRUCTURE.md                # Infrastructure documentation
└── PR_CREATION_GUIDE.md            # Pull request templates
```

---

## Development Approach: Multi-Agent Methodology

This project was developed using a **multi-agent coordination approach** with 12 specialized agents working across 4 waves:

### Wave 0: Foundation
- **AGENT-INFRA** - Spring Boot setup, Docker, CI/CD
- Created: Project structure, dependencies, PostgreSQL configuration

### Wave 1: Core Modules
- **AGENT-DATA** - JPA entities, repositories, Flyway migrations
- **AGENT-NETWORK** - Pcap4j packet capture service
- **AGENT-PROTOCOL** - Binary protocol parser (VarInt encoding)
- **AGENT-AUTOMATION** - SikuliX GUI automation framework
- **AGENT-DOCS** - Architecture and setup documentation

### Wave 2: Business Logic & API
- **AGENT-BUSINESS** - Services, caching, circuit breaker, background tasks
- **AGENT-API** - REST controllers, DTOs, OpenAPI documentation
- **AGENT-INFRA** - Docker enhancements, CI/CD pipeline (7 jobs)

### Wave 3: Frontend
- **AGENT-FRONT** - Angular 20 application with Material Design

### Quality Gates
- **AGENT-REVIEW** - Code review and quality validation after each wave
- Gate 1: Wave 1 approved (10/10)
- Gate 2: Wave 2 approved with notes (8.9/10)

---

## Key Configuration Files

### Backend Configuration

**application.yml** (base configuration):
```yaml
spring:
  application:
    name: dofus-retro-tracker
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/dofus_retro_db}
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
  cache:
    type: caffeine

dofus:
  retro:
    tracker:
      packet-capture:
        enabled: true
        dofus-port: 5555
```

**application-dev.yml** (development overrides):
```yaml
spring:
  jpa:
    show-sql: true
logging:
  level:
    com.dofusretro: DEBUG
```

**application-prod.yml** (production optimizations):
```yaml
spring:
  jpa:
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 20
logging:
  level:
    com.dofusretro: INFO
```

### Frontend Configuration

**environment.ts** (development):
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1'
};
```

**environment.prod.ts** (production):
```typescript
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1'
};
```

---

## API Endpoints

### Items API
```
GET  /api/v1/items
     Query params: page, size, search, categoryId, sort
     Returns: PagedResponse<ItemDTO>

GET  /api/v1/items/{id}
     Returns: ItemDTO

GET  /api/v1/items/{id}/prices
     Query params: page, size, startDate, endDate
     Returns: PagedResponse<PriceEntryDTO>
```

### Categories API
```
GET  /api/v1/categories
     Returns: List<CategoryDTO>

GET  /api/v1/categories/{id}
     Returns: CategoryDTO

GET  /api/v1/categories/{id}/items
     Query params: page, size, sort
     Returns: PagedResponse<ItemDTO>
```

### Health & Monitoring
```
GET  /api/v1/health
     Returns: API health status

GET  /actuator/health
     Returns: Detailed health indicators

GET  /actuator/metrics
     Returns: Micrometer metrics

GET  /actuator/prometheus
     Returns: Prometheus format metrics
```

### API Documentation
```
GET  /swagger-ui.html
     Interactive OpenAPI/Swagger documentation

GET  /v3/api-docs
     OpenAPI JSON specification
```

---

## Development Workflow

### Quick Start

**1. Start Backend:**
```bash
cd dofus-retro-tracker
docker-compose up -d          # Start PostgreSQL
mvn spring-boot:run           # Run Spring Boot app
```

**2. Start Frontend:**
```bash
cd frontend
npm install                   # First time only
npm start                     # Opens http://localhost:4200
```

**3. Access Services:**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432 (user: dofus, password: dofus_password)
- pgAdmin: http://localhost:5050 (optional, see docker-compose.yml)

### Running Tests

**Backend:**
```bash
mvn test                      # Unit tests
mvn verify                    # Unit + integration tests
mvn jacoco:report             # Generate coverage report
# Open: target/site/jacoco/index.html
```

**Frontend:**
```bash
cd frontend
npm test                      # Run Jasmine/Karma tests
npm run test:coverage         # Generate coverage report
```

### Building for Production

**Backend:**
```bash
mvn clean package             # Creates JAR in target/
java -jar target/dofus-retro-tracker-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build                 # Output: dist/frontend/
# Serve with: nginx, Apache, or Spring Boot static resources
```

**Docker:**
```bash
docker build -t dofus-retro-tracker .
docker run -p 8080:8080 dofus-retro-tracker
```

---

## Important Implementation Details

### Dofus Retro Protocol

**Message Structure:**
```
[Message ID: 2 bytes] [Message Length: VarInt] [Message Body: binary]
```

**VarInt Encoding:**
- 7-bit encoding scheme (each byte uses 7 bits for data, 1 bit for continuation)
- Used for efficient integer compression
- Implementation: `BinaryReader.readVarInt()`

**Key Message Types:**
- `EXCHANGE_TYPES_ITEMS` (5904) - List of items in auction house
- `EXCHANGE_TYPES_EXCHANGE_BUY_OK` (5905) - Purchase confirmation
- `EXCHANGE_TYPES_ITEM_PRICE` (custom) - Price data

**Note:** Message IDs for Dofus Retro may differ from Dofus 2. Validation with real packet captures is required.

### Circuit Breaker Pattern

The `PacketConsumerService` implements a circuit breaker with three states:

**CLOSED** - Normal operation
- Processes packets from queue
- Increments failure count on errors

**OPEN** - Circuit tripped (too many failures)
- Rejects all requests immediately
- Waits for recovery timeout

**HALF_OPEN** - Testing recovery
- Allows single test request
- Transitions to CLOSED if successful, OPEN if failed

**Configuration:**
```java
private static final int FAILURE_THRESHOLD = 5;
private static final Duration RECOVERY_TIMEOUT = Duration.ofMinutes(1);
```

### Caching Strategy

**Multi-level cache configuration:**

```yaml
dofus.retro.tracker.cache:
  caffeine:
    - name: items
      ttl: 3600          # 1 hour
      max-size: 10000
    - name: itemsWithPrices
      ttl: 1800          # 30 minutes
      max-size: 1000
    - name: latestPrices
      ttl: 300           # 5 minutes
      max-size: 5000
```

**Cache annotations:**
```java
@Cacheable(value = "items", key = "#id")
public Optional<Item> findById(Long id)

@CacheEvict(value = "items", key = "#item.id")
public Item save(Item item)
```

### GUI Automation State Machine

**Action Types:**
1. **SearchCategory** - Navigate to specific category
2. **ClickItem** - Click on item slot
3. **ScrollList** - Scroll through items
4. **WaitForLoad** - Wait for UI to update
5. **ReturnToStart** - Reset to initial state

**Template Matching:**
- Uses OpenCV via JavaCV
- Threshold: 0.85 (configurable)
- Supports multiple image formats (PNG, JPG)
- Platform-specific templates for HiDPI displays

---

## Environment Variables

**Backend:**
```bash
DB_URL=jdbc:postgresql://localhost:5432/dofus_retro_db
DB_USERNAME=dofus
DB_PASSWORD=dofus_password
SPRING_PROFILES_ACTIVE=dev
DOFUS_PORT=5555
CACHE_ENABLED=true
PACKET_CAPTURE_ENABLED=true
```

**Frontend:**
```bash
API_BASE_URL=http://localhost:8080/api/v1
```

**Docker Compose:**
```bash
POSTGRES_DB=dofus_retro_db
POSTGRES_USER=dofus
POSTGRES_PASSWORD=dofus_password
```

---

## Common Development Tasks

### Adding a New REST Endpoint

1. Create DTO in `dto/` package
2. Add method to repository (if needed)
3. Implement service method in `service/`
4. Create controller method in `controller/`
5. Add OpenAPI annotations (@Operation, @ApiResponse)
6. Write @WebMvcTest for controller
7. Update Angular API service

### Adding a New Database Entity

1. Create entity class in `model/` with JPA annotations
2. Create repository interface extending JpaRepository
3. Create Flyway migration in `db/migration/`
4. Add DTO and mapper
5. Write @DataJpaTest tests
6. Update service layer

### Modifying the Protocol Parser

1. Update `MessageDefinitions.java` with new message types
2. Modify `DofusRetroProtocolParser.parse()` method
3. Add parsing logic for new message structure
4. Write unit tests with sample binary data
5. Verify with real packet captures from Dofus Retro

---

## Testing Strategy

### Backend Testing

**Unit Tests:**
- Service layer: Mock repositories with Mockito
- Controllers: @WebMvcTest with MockMvc
- Utilities: Pure unit tests with JUnit 5

**Integration Tests:**
- @SpringBootTest with H2 in-memory database
- Full Spring context loading
- Test complete flows (packet → parse → persist)

**Repository Tests:**
- @DataJpaTest with H2
- Test custom queries, pagination, fetching strategies

### Frontend Testing

**Component Tests:**
- Angular TestBed for component testing
- Mock services with spies
- Test user interactions and DOM updates

**Service Tests:**
- HttpClientTestingModule for API service
- Mock HTTP responses
- Test error handling and retries

**E2E Tests (Optional):**
- Playwright for full user flows
- Test with real backend (test profile)

---

## Troubleshooting

### Packet Capture Issues

**Linux:**
```bash
sudo setcap cap_net_raw,cap_net_admin=eip /usr/bin/java
# Or run as root (not recommended)
```

**Windows:**
- Install WinPcap or Npcap
- Run with administrator privileges

**macOS:**
- Run with sudo or configure permissions
- Install libpcap via Homebrew

### Database Connection Issues

**Check PostgreSQL is running:**
```bash
docker-compose ps
docker-compose logs postgres
```

**Test connection:**
```bash
psql -h localhost -U dofus -d dofus_retro_db
# Password: dofus_password
```

### Frontend CORS Issues

**Verify backend CORS configuration:**
```java
// WebConfig.java should include:
.allowedOrigins("http://localhost:4200")
```

**Check browser console:**
- Look for CORS errors
- Verify API base URL in environment.ts

### GUI Automation Not Working

**Template Images:**
- Ensure templates are captured at correct DPI
- Check template file paths in configuration
- Verify image format (PNG recommended)

**Display Scaling:**
- Configure DPI multiplier in application.yml
- Use platform-specific templates
- Test coordinate transformation

---

## Performance Considerations

### Database Optimization

**Indexes:**
- All foreign keys indexed
- Search columns indexed (item_gid, created_at)
- Composite indexes for common queries

**Connection Pooling:**
- HikariCP configured for optimal performance
- Pool size: 20 max, 10 min idle
- Leak detection enabled in development

**Query Optimization:**
- Use @EntityGraph for fetch joins
- Implement pagination for all list endpoints
- Avoid N+1 queries with proper fetching

### Caching

**Strategy:**
- Frequently accessed items: 1 hour TTL
- Price history: 30 minutes TTL
- Latest prices: 5 minutes TTL

**Eviction:**
- Size-based eviction (max entries)
- Time-based eviction (TTL)
- Manual eviction on updates

### Frontend Performance

**Bundle Optimization:**
- Lazy loading for routes
- OnPush change detection
- Debounced search (300ms)

**API Calls:**
- Pagination for large lists
- Caching with RxJS shareReplay
- Retry logic with exponential backoff

---

## Security Considerations

### API Security

**CORS:**
- Restricted to specific origins
- Credentials allowed for authenticated requests

**Rate Limiting:**
- Bucket4j configured for 100 requests/minute
- Per-IP rate limiting (infrastructure ready)

**Input Validation:**
- Jakarta Validation on all DTOs
- SQL injection prevention via JPA parameterized queries
- XSS prevention via Angular sanitization

### Database Security

**Credentials:**
- Never commit passwords
- Use environment variables
- Rotate credentials regularly

**Access Control:**
- Database user has minimal required permissions
- No direct database access from frontend

---

## CI/CD Pipeline

**GitHub Actions Jobs:**
1. **build-and-test** - Maven build, tests, coverage
2. **code-quality** - Checkstyle, static analysis
3. **api-contract-testing** - Actuator validation
4. **docker-build-and-push** - Container builds
5. **dependency-check** - Security scanning
6. **performance-testing** - Load tests (placeholder)
7. **notification** - Status reporting

**Coverage Requirements:**
- JaCoCo minimum: 50%
- Target: 85%+
- Failing coverage fails the build

---

## Deployment

### Development Deployment

**Using Scripts:**
```bash
./scripts/run-dev.sh         # Starts Docker + app
./scripts/health-check.sh    # Verifies health
```

### Production Deployment

**Docker Compose:**
```bash
export POSTGRES_PASSWORD="strong_password"
export DB_PASSWORD="strong_password"
./scripts/run-prod.sh
```

**Kubernetes (Future):**
- Helm charts (to be created)
- ConfigMaps for configuration
- Secrets for credentials

---

## Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Angular Documentation](https://angular.dev)
- [Pcap4j Wiki](https://www.pcap4j.org/)
- [SikuliX Documentation](http://sikulix.com/)

### Project Documentation
- `DOFUS_RETRO_PRD.md` - Complete product requirements
- `IMPLEMENTATION_BOOK.md` - Task breakdown (73 tasks)
- `docs/architecture.md` - System architecture details
- `docs/setup.md` - Platform-specific setup guides
- `POSSIBLE_IMPROVEMENTS.md` - Technical debt tracking

### Tools
- **Maven Repository:** https://mvnrepository.com/
- **npm Registry:** https://www.npmjs.com/
- **Docker Hub:** https://hub.docker.com/

---

## Contributing

### Git Configuration

**IMPORTANT:** All commits to this repository must use the following author information:

```bash
git config user.name "Dragomitch"
git config user.email "orohimesama@gmail.com"
```

Or set globally:
```bash
git config --global user.name "Dragomitch"
git config --global user.email "orohimesama@gmail.com"
```

This ensures consistent authorship across all contributions.

### Code Style

**Java:**
- Google Java Style Guide
- Checkstyle enforced in CI/CD
- Javadoc for all public APIs

**TypeScript:**
- Angular Style Guide
- TSLint/ESLint configured
- Strict mode enabled

### Git Workflow

**Branches:**
- `master` - Production-ready code
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Urgent production fixes

**Commits:**
- Descriptive commit messages
- Reference issue numbers
- Atomic commits (one logical change)

### Pull Requests

**Requirements:**
- All tests passing
- Code coverage maintained
- Checkstyle compliant
- Reviewed by maintainer
- Documentation updated

---

## License & Disclaimer

**Educational Purpose Only**

This project is created for educational purposes to learn:
- Java/Spring Boot ecosystem
- Network protocol analysis
- GUI automation techniques
- Full-stack development (Java + Angular)

**Disclaimer:**
- Use at your own risk
- Not affiliated with Ankama Games
- Packet capturing may violate game Terms of Service
- Intended for local development and learning only

---

## Contact & Support

**Issues:** https://github.com/Dragomitch/HDVParserDofus2Python/issues
**Discussions:** Use GitHub Discussions for questions

---

*Last Updated: 2025-11-09*
*Version: 1.0*
*Waves Completed: 0, 1, 2, 3*

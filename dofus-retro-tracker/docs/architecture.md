# Architecture Documentation

## System Overview

The Dofus Retro Price Tracker is a distributed system designed to capture, parse, and analyze auction house (HDV - Haut du Vide) market data from the Dofus Retro game. It combines packet capture, GUI automation, and data analysis to provide comprehensive market insights.

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                  (Dofus Retro Price Tracker)                     │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │   Packet       │  │    GUI       │  │    REST API Layer    │ │
│  │   Capture      │  │  Automation  │  │                      │ │
│  │   Service      │  │   Service    │  │  ItemController      │ │
│  │ (Pcap4j)       │  │  (SikuliX)   │  │  MarketController    │ │
│  └────────┬───────┘  └──────┬───────┘  └──────────┬───────────┘ │
│           │                 │                      │              │
│           └─────────────────┼──────────────────────┘              │
│                             ↓                                    │
│                  ┌──────────────────────┐                        │
│                  │  Protocol Parser     │                        │
│                  │  (ProtocolParser)    │                        │
│                  │  - Extracts messages │                        │
│                  │  - Validates format  │                        │
│                  └──────────┬───────────┘                        │
│                             ↓                                    │
│                  ┌──────────────────────┐                        │
│                  │   Business Logic     │                        │
│                  │  (Service Layer)     │                        │
│                  │  - ItemPriceService  │                        │
│                  │  - Caching (Caffeine)│                        │
│                  │  - Validation        │                        │
│                  └──────────┬───────────┘                        │
│                             ↓                                    │
│                  ┌──────────────────────┐                        │
│                  │   Data Access        │                        │
│                  │  (Repository Layer)  │                        │
│                  │  - Spring Data JPA   │                        │
│                  │  - Transactions      │                        │
│                  └──────────┬───────────┘                        │
│                             │                                    │
└─────────────────────────────┼────────────────────────────────────┘
                              ↓
                    ┌─────────────────────┐
                    │  PostgreSQL 16      │
                    │  (Data Persistence) │
                    │  - Item catalog     │
                    │  - Price history    │
                    │  - ACID compliance  │
                    └─────────────────────┘
```

## Data Flow Architecture

### Price Capture Flow

```
Dofus Client (Game)
        ↓ (TCP packets on port 5555)
   Pcap4j Sniffer
        ↓ (raw byte[] packets)
   ProtocolParser
        ↓ (extracts HdvPriceMessage)
   ItemPriceService
        ├─ Check cache (Caffeine)
        ├─ Validate message
        └─ Persist via repository
        ↓
   PriceEntryRepository
        ↓ (JPA operations)
   PostgreSQL Database
```

### REST API Flow

```
HTTP Client Request
        ↓
   Spring DispatcherServlet
        ↓
   ItemController / MarketController
        ↓
   ItemPriceService (business logic)
        ├─ Check cache
        ├─ Query repository
        └─ Apply transformations
        ↓
   ItemRepository
        ↓ (SQL queries via Hibernate)
   PostgreSQL Database
        ↓
   JSON Response
```

## Component Architecture

### Service Layer Components

#### 1. PacketCaptureService
- **Responsibility:** Captures network packets on port 5555
- **Technologies:** Pcap4j 1.8.2
- **Key Methods:**
  - `startCapture()` - Begin packet capture
  - `stopCapture()` - Stop packet capture
  - `parsePacket(byte[])` - Delegate to ProtocolParser
- **Dependencies:** ProtocolParser, ItemPriceService

#### 2. ProtocolParser
- **Responsibility:** Parses Dofus HDV protocol messages
- **Input:** Raw byte arrays from network packets
- **Output:** HdvPriceMessage objects
- **Key Methods:**
  - `parse(byte[])` - Parse raw packet data
  - `validate(byte[])` - Validate packet structure
  - `decodeMessage(byte[])` - Decode to message objects
- **Protocol:** Dofus HDV protocol specification

#### 3. ItemPriceService
- **Responsibility:** Business logic for price tracking
- **Key Methods:**
  - `recordPrice(PriceEntry)` - Record new price entry
  - `getPriceHistory(itemId)` - Get historical prices
  - `getItemById(id)` - Retrieve item details
  - `getMarketStatistics()` - Calculate market statistics
- **Dependencies:**
  - ItemRepository (data access)
  - PriceEntryRepository (data access)
  - CacheManager (Caffeine)
- **Caching Strategy:**
  - Item catalog: 24-hour TTL
  - Recent prices: 5-minute TTL
  - Market stats: 1-hour TTL

#### 4. GuiAutomationService
- **Responsibility:** Automate GUI interactions in Dofus client
- **Technologies:** SikuliX 2.0.5, JavaCV 1.5.9
- **Key Methods:**
  - `startAutomation()` - Begin GUI automation session
  - `findTemplate(String)` - Template matching
  - `click(Point)` - Click on screen coordinate
  - `captureScreen()` - Capture game screen
- **Use Cases:**
  - Comprehensive market scanning
  - Price verification
  - Automated bidding

#### 5. REST Controllers
- **ItemController**
  - `GET /api/v1/items` - List all items
  - `GET /api/v1/items/{id}` - Get item details
  - `GET /api/v1/items/{id}/prices` - Get price history
  - `GET /api/v1/items/search` - Search items

- **MarketController**
  - `GET /api/v1/market/listings` - Current market listings
  - `GET /api/v1/market/statistics` - Market statistics
  - `GET /api/v1/market/trends` - Price trends

### Repository Layer

Uses Spring Data JPA for data access:

```
ItemRepository extends JpaRepository<Item, Long>
├─ findByName(String)
├─ findByCategoryId(Long)
└─ findAll()

SubCategoryRepository extends JpaRepository<SubCategory, Long>
└─ findByCategoryId(Long)

PriceEntryRepository extends JpaRepository<PriceEntry, Long>
├─ findByItemId(Long)
├─ findByItemIdAndTimestampBetween(...)
└─ findLatestByItemId(Long)
```

### Entity/Model Layer

#### Item Entity
```java
@Entity
@Table(name = "items")
public class Item {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private SubCategory subCategory;

    private String description;
    private Integer level;
    private String rarity;

    @OneToMany(mappedBy = "item")
    private List<PriceEntry> prices;
}
```

#### SubCategory Entity
```java
@Entity
@Table(name = "subcategories")
public class SubCategory {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_id")
    private Long categoryId;

    @OneToMany(mappedBy = "subCategory")
    private List<Item> items;
}
```

#### PriceEntry Entity
```java
@Entity
@Table(name = "price_entries", indexes = {
    @Index(name = "idx_item_timestamp", columnList = "item_id, timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class PriceEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private PriceSource source;  // PACKET_CAPTURE, GUI_AUTOMATION, MANUAL
}
```

## Database Schema

### Tables

#### items
```sql
CREATE TABLE items (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    subcategory_id BIGINT,
    description TEXT,
    level INTEGER,
    rarity VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subcategory_id) REFERENCES subcategories(id)
);
```

#### subcategories
```sql
CREATE TABLE subcategories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### price_entries
```sql
CREATE TABLE price_entries (
    id BIGINT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    price BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    source VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    INDEX idx_item_timestamp (item_id, timestamp),
    INDEX idx_timestamp (timestamp)
);
```

### Indexes

Performance-critical indexes:
- `idx_item_timestamp` - For historical price queries
- `idx_timestamp` - For time-range queries
- Primary keys on all tables for JPA operations

## Configuration Architecture

### Spring Profiles

**Development (`dev` profile)**
- H2 in-memory database option
- Debug logging
- Slower cache TTLs for testing
- GUI automation disabled by default
- Packet capture disabled by default

**Production (`prod` profile)**
- PostgreSQL external database
- Info level logging
- Optimized cache TTLs
- External configuration via environment variables
- Performance monitoring enabled

### Configuration Files

#### application.yml
Global configuration defaults

#### application-dev.yml
Development-specific overrides:
- Spring JPA: show_sql, format_sql
- Logging: DEBUG level
- Cache: shorter TTLs

#### application-prod.yml
Production settings:
- Spring JPA: optimized settings
- Logging: INFO level
- Cache: longer TTLs
- Actuator: metrics enabled
- SSL configuration (if needed)

## Technology Decisions

### Why Pcap4j for Packet Capture?

✓ Pure Java implementation - no native dependencies
✓ Cross-platform support (Windows, macOS, Linux)
✓ Active maintenance and community support
✓ Works with standard libpcap/WinPcap
✗ Performance: slower than raw sockets

**Alternative:** java.nio for raw sockets (requires OS-level support)

### Why SikuliX for GUI Automation?

✓ Template matching with image recognition
✓ OCR capabilities for dynamic content
✓ Cross-platform (Windows, macOS, Linux)
✓ Reliable for game automation
✗ Slower than direct protocol interaction

**Alternative:** Robot class (limited capabilities)

### Why Spring Boot + JPA?

✓ Rapid development with auto-configuration
✓ Built-in dependency injection
✓ Spring Data simplifies database access
✓ Mature ecosystem with extensive libraries
✓ Natural fit for microservices architecture

**Alternative:** Quarkus (faster startup, lower memory)

### Why PostgreSQL?

✓ Enterprise-grade reliability (ACID)
✓ Advanced features (JSON, arrays, full-text search)
✓ Excellent for time-series data
✓ Strong JSON support for future features
✗ Heavier than SQLite/H2

**Alternative:** TimescaleDB extension for time-series optimization

### Why Caffeine for Caching?

✓ High-performance in-memory cache
✓ Automatic expiration (TTL)
✓ Load-on-miss pattern support
✓ No external dependency (in-process)
✗ Single-JVM solution (no distributed caching)

**Alternative:** Redis (distributed, but adds complexity)

## Deployment Architecture

### Docker Containerization

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
COPY target/price-tracker-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose Orchestration

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/dofus_retro_db
    depends_on:
      - postgres

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: dofus_retro_db
      POSTGRES_PASSWORD: dofus_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

## Integration Points

### External Systems

1. **Dofus Client**
   - Protocol: TCP on port 5555
   - Data: Encrypted packet stream
   - Integration: Packet capture via Pcap4j

2. **Dofus Game Server**
   - No direct integration
   - Data obtained from client-side packet capture

3. **Monitoring Systems**
   - Prometheus metrics endpoint: `/actuator/prometheus`
   - Health check: `/actuator/health`

## Security Considerations

### Current Implementation

- No authentication/authorization (development stage)
- Packet capture requires elevated privileges
- Database password in configuration

### Future Improvements

- OAuth2/JWT authentication for API
- Role-based access control (RBAC)
- API rate limiting
- HTTPS/TLS encryption
- Secrets management (Vault)
- SQL injection prevention (already handled by JPA)

## Performance Characteristics

### Expected Throughput

- **Packet Processing:** 100+ packets/second (depends on system)
- **Price Recording:** 1000+ entries/second to database
- **API Response Time:** <100ms (cached queries)

### Optimization Strategies

- Database indexes on frequently queried columns
- Caffeine cache for item catalog and recent prices
- Connection pooling (HikariCP via Spring Boot)
- Lazy loading for JPA relationships
- Query optimization for historical price queries

## Scalability Considerations

### Current Limitations

- Single JVM instance
- In-process caching (no distribution)
- Single database connection pool

### Scaling Options

1. **Vertical Scaling**
   - Increase JVM heap size
   - Use faster hardware

2. **Horizontal Scaling** (future)
   - Multiple application instances
   - Distributed cache (Redis/Memcached)
   - Load balancer (Nginx)
   - Read replicas for PostgreSQL

3. **Database Optimization**
   - Table partitioning by date
   - Archive old price entries
   - TimescaleDB extension for time-series

## Monitoring & Observability

### Health Checks

- `/actuator/health` - Application health
- `/actuator/health/db` - Database connectivity
- `/actuator/health/packet-capture` - Packet capture service

### Metrics

- `/actuator/metrics` - All metrics
- `/actuator/prometheus` - Prometheus format

### Logging

- Logback configuration in `logback-spring.xml`
- Log files: `logs/dofus-retro-tracker.log`
- Error log: `logs/dofus-retro-tracker-error.log`
- Levels: DEBUG (dev), INFO (prod)

## Related Documentation

- [Development Setup Guide](setup.md) - How to set up development environment
- [Contributing Guidelines](../CONTRIBUTING.md) - How to contribute to the project
- [API Documentation](API.md) - REST API endpoints (upcoming)

## Architecture Evolution

### Wave 0 (COMPLETED)
- Project structure
- Spring Boot configuration
- Database setup
- CI/CD pipeline foundation

### Wave 1 (IN PROGRESS)
- Database schema and entities
- Basic REST API endpoints
- Packet capture foundation
- Service layer implementation

### Wave 2 (PLANNED)
- Complete packet parsing
- Price recording pipeline
- Market analysis features
- Advanced caching strategies

### Wave 3 (PLANNED)
- GUI automation implementation
- Automated market scanning
- User interface
- Authentication/authorization

### Wave 4 (PLANNED)
- Advanced analytics
- Machine learning for price prediction
- WebSocket API for real-time updates
- Mobile client support

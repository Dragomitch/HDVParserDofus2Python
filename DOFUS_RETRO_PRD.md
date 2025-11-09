# Product Requirements Document (PRD)
# Dofus Retro Auction House Price Tracker

**Version:** 1.0
**Date:** 2025-11-08
**Target Platform:** Java 26, Spring Boot
**Source Project:** HDVParserDofus2Python (Dofus 2)
**Target Game:** Dofus Retro

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current System Analysis](#2-current-system-analysis)
3. [Project Goals & Objectives](#3-project-goals--objectives)
4. [Functional Requirements](#4-functional-requirements)
5. [Technical Architecture](#5-technical-architecture)
6. [Data Models & Database Design](#6-data-models--database-design)
7. [Technology Stack Mapping](#7-technology-stack-mapping)
8. [Reusability Analysis](#8-reusability-analysis)
9. [Dofus Retro Specific Considerations](#9-dofus-retro-specific-considerations)
10. [API Specifications](#10-api-specifications)
11. [Non-Functional Requirements](#11-non-functional-requirements)
12. [Risk Assessment](#12-risk-assessment)
13. [Success Criteria](#13-success-criteria)
14. [Out of Scope](#14-out-of-scope)
15. [Future Enhancements](#15-future-enhancements)

---

## 1. Executive Summary

### 1.1 Purpose
Recreate the Dofus 2 Auction House (HDV) price tracking system for **Dofus Retro** using modern Java technologies (Java 26 + Spring Boot). The system will capture, store, and visualize real-time item prices from the in-game auction house for educational and analytical purposes.

### 1.2 Background
The existing Python-based system successfully monitors Dofus 2 auction house prices through:
- Network packet sniffing and parsing
- GUI automation for data collection
- SQLite database storage
- React-based web visualization

This project aims to port and enhance this functionality for Dofus Retro while leveraging Java ecosystem best practices.

### 1.3 Key Stakeholders
- **Primary Users:** Game economy analysts, data enthusiasts
- **Technical Owner:** Development team familiar with Java/Spring Boot
- **Educational Purpose:** Learning multi-threading, network protocols, database design

---

## 2. Current System Analysis

### 2.1 System Overview (Python/Dofus 2)

The existing system consists of **3 decoupled modules** communicating via threading and queues:

#### **Module 1: MITM (Man-in-the-Middle) - Packet Capture**
- **Technology:** Scapy, LaBot protocol framework
- **Function:**
  - Sniffs TCP packets on port 5555 (Dofus game server connection)
  - Filters for `ExchangeTypesItemsExchangerDescriptionForUserMessage` packets
  - Parses binary Dofus protocol using pre-computed message definitions
  - Extracts: `item_gid`, `category`, `price`, `quantity`
  - Sends data to Database module via Queue

#### **Module 2: Database - Data Persistence**
- **Technology:** SQLAlchemy ORM, SQLite
- **Function:**
  - Consumes messages from MITM queue
  - Implements 10-minute caching to prevent duplicates
  - Enriches item names from game JSON files (Items.json, ItemTypes.json)
  - Provides REST API functions for data retrieval
- **Schema:**
  ```
  Items (id, item_gid, item_name, sub_category_id)
  SubCategories (id, dofus_id, name)
  PriceEntries (id, price, quantity, item_id, created_ts)
  ```

#### **Module 3: Pixel - GUI Automation**
- **Technology:** PyAutoGUI, OpenCV
- **Function:**
  - Automates clicking through auction house interface
  - Uses template matching to locate UI elements
  - Implements state machine with 5 action types
  - Triggers packet generation for MITM capture
  - Platform-specific (macOS Retina vs Windows)

#### **Module 4: Visualization**
- **Backend:** Flask REST API
- **Frontend:** React + TypeScript, react-timeseries-charts
- **Endpoints:**
  - `/ping` - Health check
  - `/items_list` - All items for dropdown
  - `/item/<id>` - Price history time series

### 2.2 Key Technologies (Python Stack)

| Component | Technology | Lines of Code |
|-----------|-----------|---------------|
| Network Capture | Scapy | ~237 lines |
| Protocol Parsing | LaBot Framework | ~2,900 lines |
| GUI Automation | PyAutoGUI + OpenCV | ~267 lines |
| Database | SQLAlchemy + SQLite | ~156 lines |
| API Server | Flask | 31 lines |
| Frontend | React 16.13 | ~500 lines |
| Threading | Python threading + Queue | ~36 lines |

### 2.3 Data Flow

```
[Dofus Client] <-TCP-> [Game Server]
       ↓ (packets sniffed)
   [MITM Module]
       ↓ (Queue)
 [Database Module] -> [SQLite DB]
       ↑                    ↓
[Pixel Module]         [Flask API]
                            ↓
                      [React Frontend]
```

### 2.4 Current System Strengths

1. **Modular Design:** Clean separation of concerns
2. **Thread-Based Parallelism:** Independent modules with shared stop event
3. **Protocol Abstraction:** LaBot handles complex binary parsing
4. **Deduplication:** 10-minute cache prevents redundant entries
5. **Educational Value:** Well-documented learning project

### 2.5 Current System Limitations

1. **Pixel Module Slowness:** Limited by rendering speed (~30 min collection)
2. **Platform Dependency:** macOS vs Windows coordinate differences
3. **Duplicate Packets:** Receives same packet twice
4. **Limited Scope:** Resources HDV only, not items/creatures
5. **No Visualization:** Data stored but not displayed (security feature)
6. **Hardcoded Configurations:** Screen positions, port numbers

---

## 3. Project Goals & Objectives

### 3.1 Primary Goals

1. **Port to Java Ecosystem:** Migrate from Python to Java 26 + Spring Boot
2. **Target Dofus Retro:** Adapt for older Dofus version protocol
3. **Improve Performance:** Optimize packet handling and GUI automation
4. **Enhance Maintainability:** Use Java best practices and design patterns
5. **Educational Purpose:** Demonstrate modern Java development techniques

### 3.2 Secondary Goals

1. Reduce data collection time (target: <20 minutes)
2. Support multiple auction house categories (resources, items, creatures)
3. Eliminate duplicate packet processing
4. Implement cross-platform GUI automation
5. Add real-time data visualization
6. Provide configurable settings (ports, delays, categories)

### 3.3 Success Metrics

- Successfully capture and parse Dofus Retro packets
- Store price data with <1% duplication rate
- Provide REST API with <200ms response time
- Support at least 2 auction house categories
- Complete full data collection in <25 minutes
- Pass code quality metrics (SonarQube >80%)

---

## 4. Functional Requirements

### 4.1 Core Features

#### FR-1: Packet Capture & Parsing
- **Priority:** CRITICAL
- **Description:** Capture TCP packets from Dofus Retro client-server communication
- **Details:**
  - Sniff packets on configurable port (default: detect Dofus Retro port)
  - Filter for auction house exchange messages
  - Parse Dofus Retro binary protocol
  - Extract item metadata (ID, category, price, quantity)
  - Handle variable-length integers and bit-packed data
- **Acceptance Criteria:**
  - Successfully capture 100% of auction house packets
  - Parse packet data with 0% error rate
  - Support Dofus Retro protocol version X.X (TBD)

#### FR-2: Data Storage & Management
- **Priority:** CRITICAL
- **Description:** Persist price data with deduplication and enrichment
- **Details:**
  - Store items, categories, and price entries
  - Implement time-based deduplication (10 minutes)
  - Enrich item names from Dofus Retro game files
  - Support batch inserts for performance
  - Maintain historical price data
- **Acceptance Criteria:**
  - Data persisted to relational database
  - Duplicate entries reduced by >95%
  - Item names correctly mapped from game files
  - Database handles >10,000 price entries

#### FR-3: GUI Automation
- **Priority:** HIGH
- **Description:** Automate auction house navigation to trigger data packets
- **Details:**
  - Detect and click auction house categories
  - Scroll through item lists
  - Click individual items to load prices
  - Implement failsafe mechanism (manual interrupt)
  - Support configurable delays
- **Acceptance Criteria:**
  - Successfully navigate all configured categories
  - Click accuracy >98%
  - Support both Windows and Linux platforms
  - Complete full cycle in <25 minutes

#### FR-4: REST API
- **Priority:** HIGH
- **Description:** Provide HTTP API for data retrieval
- **Details:**
  - List all tracked items
  - Retrieve price history for specific item
  - Filter by date range
  - Support pagination
  - Return JSON format
- **Acceptance Criteria:**
  - RESTful design principles
  - Response time <200ms for item list
  - Support 100+ concurrent requests
  - Proper error handling (4xx, 5xx)

#### FR-5: Data Visualization
- **Priority:** MEDIUM
- **Description:** Web-based interface to view price trends
- **Details:**
  - Display time-series charts
  - Filter by item, category, date range
  - Show price statistics (min, max, avg)
  - Support multiple quantity levels (1, 10, 100)
- **Acceptance Criteria:**
  - Responsive web interface
  - Charts render in <2 seconds
  - Support modern browsers (Chrome, Firefox, Safari)

#### FR-6: Configuration Management
- **Priority:** MEDIUM
- **Description:** Externalized configuration for runtime parameters
- **Details:**
  - Network settings (port, interface)
  - Database connection
  - GUI automation timings
  - Categories to track
  - Deduplication window
- **Acceptance Criteria:**
  - Configuration via YAML/properties file
  - Environment variable overrides
  - Validation on startup
  - Hot-reload for non-critical settings

### 4.2 Feature Comparison Matrix

| Feature | Python (Dofus 2) | Java (Dofus Retro - Target) | Priority |
|---------|------------------|------------------------------|----------|
| Packet Capture | ✅ Scapy | ✅ Pcap4j / Jpcap | CRITICAL |
| Protocol Parsing | ✅ LaBot | ✅ Custom Parser | CRITICAL |
| GUI Automation | ✅ PyAutoGUI | ✅ Java Robot API / Sikuli | HIGH |
| Database | ✅ SQLite | ✅ PostgreSQL / H2 | CRITICAL |
| ORM | ✅ SQLAlchemy | ✅ JPA / Hibernate | CRITICAL |
| REST API | ✅ Flask | ✅ Spring Boot REST | HIGH |
| Frontend | ✅ React | ⚠️ Thymeleaf / React | MEDIUM |
| Threading | ✅ Python threads | ✅ ExecutorService | CRITICAL |
| Caching | ✅ Manual dict | ✅ Spring Cache / Caffeine | HIGH |
| Image Recognition | ✅ OpenCV | ✅ JavaCV / Sikuli | HIGH |
| Logging | ⚠️ Basic | ✅ SLF4J + Logback | HIGH |
| Testing | ❌ None | ✅ JUnit 5 + Mockito | HIGH |
| Configuration | ⚠️ Hardcoded | ✅ Spring Config | MEDIUM |

---

## 5. Technical Architecture

### 5.1 High-Level Architecture (Java/Spring Boot)

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐  │
│  │   Packet     │   │   Database   │   │     GUI      │  │
│  │   Capture    │   │   Service    │   │  Automation  │  │
│  │   Service    │   │   Service    │   │   Service    │  │
│  │              │   │              │   │              │  │
│  │ - Pcap4j     │   │ - JPA/Repo   │   │ - Robot API  │  │
│  │ - Protocol   │   │ - Caching    │   │ - Sikuli     │  │
│  │   Parser     │   │ - Enrichment │   │ - Actions    │  │
│  └──────┬───────┘   └──────┬───────┘   └──────────────┘  │
│         │                   │                              │
│         └───────────────────┴──────────────┐              │
│                                             ↓              │
│                        ┌──────────────────────────────┐   │
│                        │   BlockingQueue              │   │
│                        │   (Packet → DB)              │   │
│                        └──────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              REST API Controller                     │ │
│  │  - GET /api/items                                    │ │
│  │  - GET /api/items/{id}/prices                       │ │
│  │  - GET /api/health                                   │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                    Data Access Layer                       │
│              (Spring Data JPA + Hibernate)                 │
├─────────────────────────────────────────────────────────────┤
│                    PostgreSQL / H2                         │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Component Design

#### 5.2.1 Packet Capture Service
```java
@Service
public class PacketCaptureService {
    private final BlockingQueue<PacketData> packetQueue;
    private final ProtocolParser parser;
    private PcapHandle handle;

    @PostConstruct
    public void startCapture() {
        // Initialize pcap handle
        // Filter for Dofus Retro traffic
        // Parse and enqueue packets
    }

    @PreDestroy
    public void stopCapture() {
        // Graceful shutdown
    }
}
```

#### 5.2.2 Protocol Parser
```java
public class DofusRetroProtocolParser {
    private final Map<Integer, MessageDefinition> messageDefinitions;

    public ParsedMessage parse(byte[] rawPacket) {
        // Read message ID
        // Lookup message structure
        // Parse binary data (VarInt, BitPacked, etc.)
        // Return structured data
    }
}
```

#### 5.2.3 Database Service
```java
@Service
public class ItemPriceService {
    private final ItemRepository itemRepository;
    private final PriceEntryRepository priceRepository;
    private final Cache<String, LocalDateTime> deduplicationCache;

    @Async
    public void processPacket(PacketData packet) {
        // Check deduplication cache
        // Enrich item data
        // Save to database
        // Update cache
    }
}
```

#### 5.2.4 GUI Automation Service
```java
@Service
public class AuctionHouseAutomation {
    private final Robot robot;
    private final ActionStateMachine stateMachine;

    @Scheduled(fixedDelay = 100)
    public void executeNextAction() {
        Action action = stateMachine.getCurrentAction();
        action.execute(robot);
        stateMachine.transition(action.getResult());
    }
}
```

### 5.3 Threading Model

**Java Concurrent Utilities:**
```java
@Configuration
public class AsyncConfiguration {
    @Bean(name = "packetExecutor")
    public Executor packetExecutor() {
        return new ThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100)
        );
    }

    @Bean(name = "databaseExecutor")
    public Executor databaseExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean(name = "automationExecutor")
    public Executor automationExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
```

### 5.4 Communication Patterns

1. **Packet → Database:** BlockingQueue (Producer-Consumer)
2. **GUI → System:** Event-driven (Spring Events)
3. **API → Database:** Synchronous (Spring Data JPA)
4. **Inter-Module:** ApplicationEventPublisher

---

## 6. Data Models & Database Design

### 6.1 Entity Relationship Diagram

```
┌─────────────────┐
│   SubCategory   │
│─────────────────│
│ id (PK)         │
│ dofusId         │
│ name            │
└────────┬────────┘
         │
         │ 1:N
         ↓
┌─────────────────┐
│      Item       │
│─────────────────│
│ id (PK)         │
│ itemGid         │◄────┐
│ itemName        │     │
│ subCategoryId   │     │
│ createdAt       │     │
│ updatedAt       │     │
└────────┬────────┘     │
         │              │
         │ 1:N          │
         ↓              │
┌─────────────────┐     │
│   PriceEntry    │     │
│─────────────────│     │
│ id (PK)         │     │
│ itemId (FK)     │─────┘
│ price           │
│ quantity        │
│ createdAt       │
│ serverTimestamp │
└─────────────────┘
```

### 6.2 JPA Entity Definitions

#### 6.2.1 Item Entity
```java
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_gid", columnList = "item_gid")
})
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_gid", nullable = false)
    private Integer itemGid;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<PriceEntry> prices;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters, setters, equals, hashCode
}
```

#### 6.2.2 SubCategory Entity
```java
@Entity
@Table(name = "sub_categories")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dofus_id", unique = true)
    private Integer dofusId;

    @Column(name = "name", length = 100)
    private String name;

    @OneToMany(mappedBy = "subCategory")
    private List<Item> items;

    // Getters, setters
}
```

#### 6.2.3 PriceEntry Entity
```java
@Entity
@Table(name = "price_entries", indexes = {
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_item_quantity", columnList = "item_id, quantity")
})
public class PriceEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "server_timestamp")
    private Long serverTimestamp;

    // Getters, setters
}
```

### 6.3 Database Schema (PostgreSQL)

```sql
CREATE TABLE sub_categories (
    id BIGSERIAL PRIMARY KEY,
    dofus_id INTEGER UNIQUE NOT NULL,
    name VARCHAR(100)
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    item_gid INTEGER NOT NULL,
    item_name VARCHAR(255),
    sub_category_id BIGINT REFERENCES sub_categories(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_item_gid ON items(item_gid);

CREATE TABLE price_entries (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    price BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    server_timestamp BIGINT
);

CREATE INDEX idx_created_at ON price_entries(created_at);
CREATE INDEX idx_item_quantity ON price_entries(item_id, quantity);
```

### 6.4 Repository Interfaces

```java
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByItemGid(Integer itemGid);
    List<Item> findBySubCategory(SubCategory category);
}

public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {
    List<PriceEntry> findByItemAndCreatedAtAfter(
        Item item,
        LocalDateTime after
    );

    @Query("SELECT p FROM PriceEntry p WHERE p.item.id = :itemId " +
           "AND p.createdAt >= :startDate " +
           "ORDER BY p.createdAt ASC")
    List<PriceEntry> findPriceHistory(
        @Param("itemId") Long itemId,
        @Param("startDate") LocalDateTime startDate
    );
}
```

---

## 7. Technology Stack Mapping

### 7.1 Core Technologies

| Function | Python (Original) | Java (Target) | Justification |
|----------|-------------------|---------------|---------------|
| **Language** | Python 3.8 | Java 26 | Modern LTS, virtual threads, pattern matching |
| **Framework** | N/A | Spring Boot 3.3 | Dependency injection, auto-configuration |
| **Build Tool** | pip | Maven 3.9 / Gradle 8 | Dependency management, multi-module |
| **Database** | SQLite | PostgreSQL 16 / H2 | Production-ready, better concurrency |
| **ORM** | SQLAlchemy | Hibernate 6.4 (JPA) | Standard, type-safe queries |
| **Packet Capture** | Scapy | Pcap4j 1.8 | Pure Java, cross-platform |
| **GUI Automation** | PyAutoGUI | Robot API + Sikuli | Native Java, image recognition |
| **Image Processing** | OpenCV-Python | JavaCV (OpenCV wrapper) | Template matching |
| **Threading** | threading + Queue | ExecutorService + BlockingQueue | Robust, scalable |
| **HTTP Server** | Flask | Spring WebMVC | RESTful, annotation-based |
| **Caching** | Manual dict | Caffeine / Spring Cache | High performance, expiration |
| **Logging** | logging | SLF4J + Logback | Structured, configurable |
| **Config** | Hardcoded | Spring Config + YAML | External, environment-aware |
| **Testing** | None | JUnit 5 + Mockito | Unit, integration tests |

### 7.2 Java 26 Specific Features

#### 7.2.1 Virtual Threads (JEP 444)
```java
@Configuration
public class VirtualThreadConfig {
    @Bean
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

#### 7.2.2 Pattern Matching for Switch (JEP 441)
```java
public String formatPrice(Object value) {
    return switch (value) {
        case Integer i -> String.format("%d K", i / 1000);
        case Long l -> String.format("%d K", l / 1000);
        case null -> "N/A";
        default -> value.toString();
    };
}
```

#### 7.2.3 Sequenced Collections (JEP 431)
```java
SequencedCollection<PriceEntry> prices = priceRepository.findRecent();
PriceEntry latest = prices.getLast();
```

### 7.3 Spring Boot Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Packet Capture -->
    <dependency>
        <groupId>org.pcap4j</groupId>
        <artifactId>pcap4j-core</artifactId>
        <version>1.8.2</version>
    </dependency>
    <dependency>
        <groupId>org.pcap4j</groupId>
        <artifactId>pcap4j-packetfactory-static</artifactId>
        <version>1.8.2</version>
    </dependency>

    <!-- GUI Automation -->
    <dependency>
        <groupId>org.sikuli</groupId>
        <artifactId>sikulixapi</artifactId>
        <version>2.0.5</version>
    </dependency>

    <!-- Image Processing -->
    <dependency>
        <groupId>org.bytedeco</groupId>
        <artifactId>javacv-platform</artifactId>
        <version>1.5.9</version>
    </dependency>

    <!-- Caching -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>

    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>33.0.0-jre</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 7.4 Project Structure

```
dofus-retro-price-tracker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dofusretro/pricetracker/
│   │   │       ├── DofusRetroApplication.java
│   │   │       ├── config/
│   │   │       │   ├── AsyncConfig.java
│   │   │       │   ├── CacheConfig.java
│   │   │       │   └── PcapConfig.java
│   │   │       ├── service/
│   │   │       │   ├── PacketCaptureService.java
│   │   │       │   ├── ProtocolParserService.java
│   │   │       │   ├── ItemPriceService.java
│   │   │       │   └── AuctionHouseAutomationService.java
│   │   │       ├── repository/
│   │   │       │   ├── ItemRepository.java
│   │   │       │   ├── PriceEntryRepository.java
│   │   │       │   └── SubCategoryRepository.java
│   │   │       ├── model/
│   │   │       │   ├── Item.java
│   │   │       │   ├── PriceEntry.java
│   │   │       │   └── SubCategory.java
│   │   │       ├── dto/
│   │   │       │   ├── PacketData.java
│   │   │       │   ├── ItemPriceDTO.java
│   │   │       │   └── PriceHistoryDTO.java
│   │   │       ├── controller/
│   │   │       │   └── ItemPriceController.java
│   │   │       ├── protocol/
│   │   │       │   ├── DofusRetroProtocol.java
│   │   │       │   ├── MessageDefinition.java
│   │   │       │   └── BinaryReader.java
│   │   │       └── automation/
│   │   │           ├── Action.java
│   │   │           ├── ActionStateMachine.java
│   │   │           └── TemplateMatching.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── templates/
│   │       │   └── (UI element images)
│   │       └── static/
│   │           └── (web assets)
│   └── test/
│       └── java/
│           └── com/dofusretro/pricetracker/
│               ├── service/
│               ├── repository/
│               └── integration/
├── pom.xml
├── README.md
└── docker-compose.yml
```

---

## 8. Reusability Analysis

### 8.1 Directly Reusable Concepts

✅ **Architecture Pattern**
- 3-module separation (Capture, Database, Automation)
- Producer-consumer queue pattern
- Thread-based parallelism
- Graceful shutdown mechanism

✅ **Data Model**
- Entity relationships (Item ↔ Category ↔ Price)
- Deduplication cache strategy (10-minute window)
- Price entry schema (quantity levels: 1, 10, 100)

✅ **Business Logic**
- Auction house navigation flow
- Item name enrichment from game files
- Price history time-series storage

✅ **API Design**
- REST endpoints structure
- Item listing and detail retrieval
- Health check endpoint

### 8.2 Adaptable with Modification

⚠️ **Protocol Parsing**
- **Original:** Dofus 2 protocol (LaBot framework)
- **Required:** Reverse-engineer Dofus Retro protocol
- **Differences:** Likely older message IDs, simpler data structures
- **Approach:**
  - Use PyDofus equivalent for Dofus Retro
  - Manual packet inspection with Wireshark
  - Build custom parser based on discovered format

⚠️ **GUI Automation Coordinates**
- **Original:** macOS Retina / Windows specific
- **Required:** Platform-agnostic approach
- **Approach:**
  - Use Sikuli for template-based positioning
  - Store multiple template images per platform
  - Implement coordinate normalization

⚠️ **Game Data Files**
- **Original:** Dofus 2 Items.json, ItemTypes.json
- **Required:** Extract from Dofus Retro client
- **Approach:**
  - Use Dofus Retro SWF decompiler
  - Export to JSON format
  - Map GID → Name

### 8.3 Not Reusable (Technology-Specific)

❌ **Python Libraries**
- Scapy → Pcap4j (complete rewrite)
- PyAutoGUI → Java Robot API (different API)
- SQLAlchemy → JPA/Hibernate (different paradigm)
- Flask → Spring Boot (framework change)

❌ **Threading Implementation**
- Python threading → Java ExecutorService (different concurrency model)
- Python Queue → BlockingQueue (different API)

❌ **LaBot Framework**
- Python-specific protocol parser
- Must rebuild in Java or use existing Java Dofus library

### 8.4 Reusability Matrix

| Component | Reusability | Effort | Notes |
|-----------|-------------|--------|-------|
| Architecture | 90% | Low | Core design translates well |
| Data Model | 95% | Low | JPA entities map cleanly |
| API Endpoints | 80% | Low | Spring REST similar to Flask |
| Packet Capture | 30% | High | Different library, same concept |
| Protocol Parser | 10% | Very High | Must reverse-engineer Retro |
| GUI Automation | 40% | High | Different API, same logic |
| Database Schema | 100% | None | SQL translates directly |
| Configuration | 50% | Medium | Different format (YAML vs hardcoded) |
| Testing | 0% | Medium | None exists in original |
| Documentation | 70% | Low | Concepts apply, update tech stack |

---

## 9. Dofus Retro Specific Considerations

### 9.1 Key Differences from Dofus 2

#### 9.1.1 Protocol Differences
| Aspect | Dofus 2 | Dofus Retro | Impact |
|--------|---------|-------------|--------|
| **Port** | 5555 | ~5555 (TBD) | Must verify actual port |
| **Message Format** | Complex, evolved | Simpler, older | Easier parsing |
| **Message IDs** | Current IDs | Legacy IDs | Must reverse-engineer |
| **Encryption** | Possible | Likely none/basic | Easier capture |
| **Compression** | Used | Minimal | Simpler decompression |

#### 9.1.2 Client Differences
- **UI Layout:** Older interface, different coordinates
- **Item Count:** Fewer items than Dofus 2
- **Categories:** Different organization
- **Rendering Speed:** Potentially slower

#### 9.1.3 Game Data
- **Item Database:** Smaller, older items
- **JSON Format:** May not exist, need SWF extraction
- **Category Structure:** Simpler hierarchy

### 9.2 Protocol Reverse Engineering Strategy

#### Phase 1: Packet Capture
```bash
# Use Wireshark to capture Dofus Retro traffic
tcpdump -i any -w dofus-retro.pcap port 5555
```

#### Phase 2: Pattern Analysis
1. Identify message header structure
2. Map message IDs to actions (login, move, exchange)
3. Analyze HDV-specific packets
4. Document variable-length encoding

#### Phase 3: Parser Implementation
```java
public class DofusRetroProtocolParser {
    public ParsedMessage parse(ByteBuffer buffer) {
        int messageId = readVarInt(buffer);
        int messageLength = readVarInt(buffer);

        return switch (messageId) {
            case MSG_HDV_ITEMS -> parseHdvItems(buffer, messageLength);
            case MSG_HDV_PRICES -> parseHdvPrices(buffer, messageLength);
            default -> null;
        };
    }

    private int readVarInt(ByteBuffer buffer) {
        // Implement variable-length integer decoding
    }
}
```

### 9.3 Item Data Extraction

**Option 1: Manual Extraction**
```bash
# Decompile Dofus Retro SWF
ffdec DofusRetro.swf -export script ./output

# Find item definitions
grep -r "item_" ./output/scripts/
```

**Option 2: Use Existing Tools**
- Dofus Retro API (if available)
- Community databases (dofusretro.org, etc.)

**Option 3: In-Game Scraping**
- Use GUI automation to read item tooltips
- OCR for item names
- Less reliable, fallback option

### 9.4 Configuration for Dofus Retro

```yaml
# application.yml
dofus:
  retro:
    version: "1.29.1"
    network:
      port: 5555
      interface: "any"

    protocol:
      messages:
        hdv-items: 0x1234  # TBD: actual message ID
        hdv-prices: 0x5678

    automation:
      delays:
        click: 200ms
        scroll: 300ms
        category-switch: 500ms

      coordinates:
        category-list:
          x: 50
          y: 150
          width: 200
          height: 400
        item-list:
          x: 300
          y: 150
          width: 400
          height: 400

    data:
      items-file: "classpath:dofus-retro/Items.json"
      categories-file: "classpath:dofus-retro/ItemTypes.json"
```

### 9.5 Testing Strategy for Retro

1. **Packet Validation:** Compare captured packets with Wireshark
2. **Protocol Tests:** Unit test parser with known packet samples
3. **GUI Tests:** Verify click coordinates on different resolutions
4. **Integration Tests:** Full flow with Dofus Retro test server
5. **Performance:** Measure collection time vs Python version

---

## 10. API Specifications

### 10.1 REST Endpoints

#### 10.1.1 Health Check
```
GET /api/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-11-08T10:30:00Z",
  "components": {
    "database": "UP",
    "packetCapture": "UP",
    "automation": "RUNNING"
  }
}
```

#### 10.1.2 List All Items
```
GET /api/items?category={categoryId}&page={page}&size={size}
```

**Parameters:**
- `category` (optional): Filter by category ID
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Page size

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "itemGid": 289,
      "name": "Blé",
      "category": {
        "id": 1,
        "dofusId": 48,
        "name": "Céréales"
      },
      "latestPrice": {
        "price": 15,
        "quantity": 1,
        "timestamp": "2025-11-08T10:25:00Z"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

#### 10.1.3 Get Item Price History
```
GET /api/items/{itemId}/prices?from={startDate}&to={endDate}&quantity={qty}
```

**Parameters:**
- `itemId` (required): Item ID
- `from` (optional): Start date (ISO 8601)
- `to` (optional): End date (ISO 8601)
- `quantity` (optional): Filter by quantity (1, 10, 100)

**Response:**
```json
{
  "itemId": 1,
  "itemName": "Blé",
  "prices": [
    {
      "timestamp": "2025-11-08T10:00:00Z",
      "quantity": 1,
      "price": 15
    },
    {
      "timestamp": "2025-11-08T10:10:00Z",
      "quantity": 10,
      "price": 140
    },
    {
      "timestamp": "2025-11-08T10:20:00Z",
      "quantity": 100,
      "price": 1300
    }
  ],
  "statistics": {
    "min": 15,
    "max": 20,
    "avg": 17.5,
    "median": 17
  }
}
```

#### 10.1.4 Get Categories
```
GET /api/categories
```

**Response:**
```json
[
  {
    "id": 1,
    "dofusId": 48,
    "name": "Céréales",
    "itemCount": 8
  },
  {
    "id": 2,
    "dofusId": 58,
    "name": "Poissons",
    "itemCount": 42
  }
]
```

### 10.2 Controller Implementation

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemPriceController {

    private final ItemPriceService itemPriceService;

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        return ResponseEntity.ok(itemPriceService.getHealthStatus());
    }

    @GetMapping("/items")
    public ResponseEntity<Page<ItemDTO>> getItems(
            @RequestParam(required = false) Long category,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ItemDTO> items = category != null
            ? itemPriceService.getItemsByCategory(category, pageable)
            : itemPriceService.getAllItems(pageable);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{itemId}/prices")
    public ResponseEntity<PriceHistoryDTO> getPriceHistory(
            @PathVariable Long itemId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) Integer quantity) {

        PriceHistoryDTO history = itemPriceService.getPriceHistory(
            itemId, from, to, quantity
        );

        return ResponseEntity.ok(history);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        return ResponseEntity.ok(itemPriceService.getAllCategories());
    }
}
```

### 10.3 Error Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(
            ItemNotFoundException ex) {

        ErrorResponse error = new ErrorResponse(
            404,
            "Item not found",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log and return 500
    }
}
```

---

## 11. Non-Functional Requirements

### 11.1 Performance

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Packet Processing** | <10ms per packet | p95 latency |
| **Database Insert** | <50ms batch insert | Average time |
| **API Response Time** | <200ms | p95 latency |
| **Full Collection Time** | <25 minutes | End-to-end |
| **GUI Click Accuracy** | >98% | Success rate |
| **Memory Usage** | <512MB heap | Max RSS |
| **CPU Usage** | <30% avg | During collection |

### 11.2 Scalability

- **Database:** Support >100,000 price entries
- **Concurrent API:** Handle 100+ concurrent requests
- **Packet Queue:** Buffer up to 1,000 packets
- **Cache Size:** 10,000 deduplication entries

### 11.3 Reliability

- **Uptime:** 99% during collection period
- **Data Accuracy:** 100% packet parsing accuracy
- **Error Recovery:** Auto-restart on crash
- **Data Integrity:** ACID transactions for price entries

### 11.4 Security

- **Network:** Read-only packet capture (no injection)
- **Database:** Parameterized queries (SQL injection prevention)
- **API:** CORS configuration for allowed origins
- **Logging:** No sensitive data (credentials) in logs

### 11.5 Maintainability

- **Code Coverage:** >80% unit test coverage
- **Code Quality:** SonarQube rating A
- **Documentation:** JavaDoc for public APIs
- **Logging:** Structured logging (JSON format)

### 11.6 Usability

- **Configuration:** Single YAML file
- **Startup:** <30 seconds to ready state
- **Monitoring:** Health endpoint + metrics
- **Failsafe:** Manual interrupt (Ctrl+C)

### 11.7 Compatibility

- **OS:** Windows 10+, Linux (Ubuntu 20.04+), macOS 12+
- **Java:** Java 26+
- **Database:** PostgreSQL 14+, H2 (testing)
- **Browser:** Chrome 90+, Firefox 88+ (for UI)

---

## 12. Risk Assessment

### 12.1 Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Dofus Retro protocol undocumented** | High | High | Allocate 2 weeks for reverse engineering; use Wireshark extensively |
| **Pcap4j platform issues** | Medium | Medium | Test on all target platforms early; have fallback (JNetPcap) |
| **Sikuli performance** | Medium | Low | Benchmark early; optimize template matching |
| **Database performance** | Low | Medium | Use connection pooling; add indexes |
| **GUI automation detection** | Low | High | Add random delays; mimic human behavior |
| **Java 26 stability** | Low | Low | Use LTS version (Java 21) if issues arise |

### 12.2 Project Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Scope creep** | Medium | Medium | Strict PRD adherence; MVP first |
| **Protocol changes** | Low | High | Monitor Dofus Retro updates; version-lock |
| **Limited Retro resources** | High | Medium | Engage community; use forums |
| **Testing complexity** | Medium | Medium | Automated tests; Docker test environment |

### 12.3 Legal/Ethical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **ToS violation** | High | High | **Educational use only**; clear disclaimers |
| **Ban from game** | High | Medium | Test on isolated account; expect bans |
| **Misuse by others** | Medium | High | Private repo; no public distribution |

**Important:** This project is for **educational purposes only**. Using bots violates Dofus ToS and harms the game economy.

---

## 13. Success Criteria

### 13.1 Minimum Viable Product (MVP)

✅ **Must Have (Release 1.0):**
1. Capture Dofus Retro auction house packets
2. Parse at least 1 category of items (resources)
3. Store data in PostgreSQL with deduplication
4. Provide REST API for item list and price history
5. GUI automation for 1 category
6. Run on Windows + Linux
7. Complete collection in <30 minutes
8. 80% code coverage

### 13.2 Enhanced Features (Release 1.1)

🎯 **Should Have:**
1. Support 3+ auction house categories
2. Real-time web visualization (charts)
3. Configurable via YAML
4. Docker deployment
5. Performance <20 minutes collection
6. Cross-platform GUI (Mac support)

### 13.3 Advanced Features (Release 2.0)

💡 **Nice to Have:**
1. WebSocket real-time updates
2. Price alerts (email/webhook)
3. Machine learning price predictions
4. Export data (CSV, Excel)
5. Multiple server support
6. Distributed architecture (Kafka)

### 13.4 Acceptance Testing

**Test 1: Packet Capture**
- Start application
- Open Dofus Retro auction house
- Click 5 items
- Verify 5 packets captured and parsed

**Test 2: Database**
- Run full collection
- Query database
- Verify >100 price entries
- Verify <2% duplicates

**Test 3: API**
- GET /api/items
- Verify response <200ms
- Verify correct JSON format

**Test 4: GUI Automation**
- Start automation
- Monitor 10 minutes
- Verify >50 items clicked
- Verify no errors

---

## 14. Out of Scope

The following features are explicitly **NOT** included in this project:

❌ **Automated Trading**
- No buy/sell execution
- No profit calculation
- No arbitrage logic

❌ **Real-Time Bots**
- No continuous 24/7 operation
- No combat bots
- No farming bots

❌ **Multi-Account**
- No account management
- No proxy support

❌ **Anti-Detection**
- No CAPTCHA solving
- No behavior randomization beyond basic delays
- No packet encryption bypass

❌ **Public Deployment**
- No hosted service
- No public API
- No SaaS offering

❌ **Mobile Support**
- No Android/iOS apps

❌ **Advanced Analytics**
- No ML models (beyond basic stats)
- No predictive algorithms
- No market manipulation

---

## 15. Future Enhancements

### 15.1 Phase 2 (Post-MVP)

**Backend:**
- WebSocket support for live updates
- GraphQL API alternative
- Redis caching layer
- Kafka for event streaming

**Frontend:**
- React dashboard with recharts
- Filter by price range
- Export to Excel/CSV
- Responsive mobile design

**Data:**
- Price prediction ML model (LSTM)
- Anomaly detection (unusual prices)
- Category-wide statistics

**Infrastructure:**
- Kubernetes deployment
- Prometheus + Grafana monitoring
- CI/CD pipeline (GitHub Actions)

### 15.2 Phase 3 (Advanced)

**Distributed Architecture:**
```
[Multiple Collectors] → [Kafka] → [Stream Processor] → [Time-Series DB]
                                          ↓
                                    [Analytics Engine]
```

**Features:**
- Multi-server support (different Dofus Retro servers)
- Historical trend analysis
- API rate limiting
- User authentication (OAuth2)
- Webhook alerts

### 15.3 Community Contributions

**Potential Extensions:**
- Item rarity calculator
- Craft profit analyzer
- Market trend reports
- Discord bot integration

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **HDV** | Hôtel de Vente (Auction House in French) |
| **GID** | Game Item ID (unique identifier) |
| **MITM** | Man-in-the-Middle (packet interception) |
| **Pcap** | Packet Capture (network monitoring) |
| **VarInt** | Variable-length integer encoding |
| **Dofus Retro** | Legacy version of Dofus MMORPG (v1.x) |
| **LaBot** | Python framework for Dofus protocol parsing |
| **JPA** | Java Persistence API (ORM standard) |
| **DTO** | Data Transfer Object |

---

## Appendix B: References

### Documentation
1. **Dofus 2 Project:** https://github.com/{original-repo}
2. **LaBot Framework:** https://github.com/louisabraham/LaBot
3. **Spring Boot Docs:** https://spring.io/projects/spring-boot
4. **Pcap4j:** https://github.com/kaitoy/pcap4j
5. **Sikuli:** http://sikulix.com/

### Tools
- **Wireshark:** Packet analysis
- **PyDofus:** Game data extraction
- **FFDEC:** SWF decompiler
- **Postman:** API testing

### Communities
- Dofus Retro Forums
- Dofus Protocol Research Discord
- r/Dofus subreddit

---

## Appendix C: Change Log

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2025-11-08 | Initial PRD creation | Claude |

---

## Appendix D: Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | TBD | _________ | ____ |
| Tech Lead | TBD | _________ | ____ |
| QA Lead | TBD | _________ | ____ |

---

**End of Document**

---

## Next Steps

1. **Review & Approval:** Stakeholders review this PRD
2. **Technical Spike:** 2-week protocol reverse engineering
3. **Architecture Design:** Detailed class diagrams
4. **Sprint Planning:** Break into 2-week sprints
5. **Development:** Start with Packet Capture module
6. **Testing:** Continuous integration setup
7. **Deployment:** Docker + PostgreSQL
8. **Documentation:** User guide + API docs

**Estimated Timeline:** 8-12 weeks for MVP

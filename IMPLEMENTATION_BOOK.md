# Implementation Book
# Dofus Retro Price Tracker - Multi-Agent Execution Plan

**Version:** 1.0
**Date:** 2025-11-08
**Project:** Dofus Retro Auction House Price Tracker (Java 26 + Spring Boot)
**Based on:** DOFUS_RETRO_PRD.md

---

## 📋 Table of Contents

1. [Agent Profiles](#1-agent-profiles)
2. [Execution Strategy](#2-execution-strategy)
3. [Task Breakdown by Phase](#3-task-breakdown-by-phase)
4. [Dependency Matrix](#4-dependency-matrix)
5. [Implementation Roadmap](#5-implementation-roadmap)
6. [Task Catalog](#6-task-catalog)
7. [Coordination Protocol](#7-coordination-protocol)
8. [Integration Points](#8-integration-points)
9. [Success Validation](#9-success-validation)

---

## 1. Agent Profiles

### 1.1 Agent Definitions

Each agent has specialized knowledge and responsibilities. Agents can work in parallel when tasks are independent.

#### 🏗️ AGENT-INFRA: Infrastructure & Setup Specialist
**Expertise:** Project structure, build tools, dependencies, configuration
**Responsibilities:**
- Maven/Gradle project setup
- Spring Boot application structure
- Docker configuration
- Database initialization scripts
- CI/CD pipelines
**Key Skills:** Maven, Spring Boot, Docker, PostgreSQL
**Works Best With:** No dependencies initially

---

#### 🗄️ AGENT-DATA: Database & ORM Specialist
**Expertise:** JPA entities, database schema, repositories
**Responsibilities:**
- Entity class design
- Repository interfaces
- Database migrations (Flyway/Liquibase)
- Query optimization
- Test data generation
**Key Skills:** JPA/Hibernate, PostgreSQL, Spring Data
**Dependencies:** AGENT-INFRA (project structure)

---

#### 📡 AGENT-NETWORK: Network & Packet Capture Specialist
**Expertise:** Network protocols, packet sniffing, Pcap4j
**Responsibilities:**
- Pcap4j integration
- Packet capture service
- Network filter configuration
- TCP/IP layer handling
- Packet queue implementation
**Key Skills:** Pcap4j, network programming, binary protocols
**Dependencies:** AGENT-INFRA (project structure)

---

#### 🔐 AGENT-PROTOCOL: Protocol Parsing Specialist
**Expertise:** Binary parsing, Dofus Retro protocol, data extraction
**Responsibilities:**
- Reverse engineer Dofus Retro protocol
- Binary reader/writer implementation
- Message definition classes
- Protocol parser service
- Unit tests for parsing logic
**Key Skills:** Binary protocols, reverse engineering, Wireshark
**Dependencies:** AGENT-NETWORK (packet structure understanding)

---

#### 🤖 AGENT-AUTOMATION: GUI Automation Specialist
**Expertise:** Robot API, Sikuli, image recognition
**Responsibilities:**
- GUI automation service
- Action state machine
- Template matching implementation
- Cross-platform coordinate handling
- Failsafe mechanisms
**Key Skills:** Java Robot, JavaCV/Sikuli, OpenCV
**Dependencies:** AGENT-INFRA (project structure)

---

#### 💼 AGENT-BUSINESS: Business Logic Specialist
**Expertise:** Service layer, caching, data enrichment
**Responsibilities:**
- ItemPriceService implementation
- Deduplication cache logic
- Item name enrichment
- Queue processing
- Business rules
**Key Skills:** Spring Service, Caffeine cache, async processing
**Dependencies:** AGENT-DATA (entities), AGENT-PROTOCOL (parsed data)

---

#### 🌐 AGENT-API: REST API Specialist
**Expertise:** Spring REST, controllers, DTOs
**Responsibilities:**
- REST controller implementation
- DTO design
- API documentation (OpenAPI)
- Error handling
- Pagination logic
**Key Skills:** Spring WebMVC, REST design, OpenAPI
**Dependencies:** AGENT-BUSINESS (service layer)

---

#### 🧪 AGENT-TEST: Testing & Quality Specialist
**Expertise:** JUnit, Mockito, integration tests
**Responsibilities:**
- Unit test coverage
- Integration tests
- Mock implementations
- Test data builders
- Performance tests
**Key Skills:** JUnit 5, Mockito, TestContainers, AssertJ
**Dependencies:** All other agents (tests their code)

---

#### 📚 AGENT-DOCS: Documentation Specialist
**Expertise:** Technical writing, API documentation, guides
**Responsibilities:**
- README updates
- API documentation
- Developer guides
- Architecture diagrams
- Deployment instructions
**Key Skills:** Markdown, Mermaid diagrams, OpenAPI
**Dependencies:** All other agents (documents their work)

---

### 1.2 Agent Capability Matrix

| Agent | Can Work Solo | Needs Input From | Outputs For |
|-------|---------------|------------------|-------------|
| AGENT-INFRA | ✅ Yes | - | ALL |
| AGENT-DATA | ⚠️ After INFRA | INFRA | BUSINESS, API, TEST |
| AGENT-NETWORK | ⚠️ After INFRA | INFRA | PROTOCOL, BUSINESS |
| AGENT-PROTOCOL | ⚠️ After NETWORK | NETWORK, DATA | BUSINESS, TEST |
| AGENT-AUTOMATION | ⚠️ After INFRA | INFRA | BUSINESS, TEST |
| AGENT-BUSINESS | ❌ Needs multiple | DATA, PROTOCOL | API, TEST |
| AGENT-API | ❌ Needs BUSINESS | BUSINESS, DATA | TEST, DOCS |
| AGENT-TEST | ❌ Needs code | ALL | Quality assurance |
| AGENT-DOCS | ⚠️ Ongoing | ALL | Documentation |

---

## 2. Execution Strategy

### 2.1 Parallel Execution Principles

**Maximize Parallelization:**
1. **Identify independent tracks** - Tasks with no shared dependencies
2. **Create interface contracts early** - Allows parallel development
3. **Use mocks/stubs** - Don't wait for real implementations
4. **Synchronize at integration points** - Defined merge moments

**Execution Waves:**
```
WAVE 0 (Foundation) → WAVE 1 (Core Modules) → WAVE 2 (Integration) → WAVE 3 (Polish)
     ↓                      ↓                         ↓                    ↓
  Sequential          High Parallelism          Medium Parallel      Low Parallel
```

### 2.2 Critical Path

The **longest dependency chain** determines minimum project duration:

```
INFRA → NETWORK → PROTOCOL → BUSINESS → API → TEST → DEPLOY
  1w      1w        2w         1w        1w     1w     1w  = 8 weeks
```

### 2.3 Parallelization Opportunities

**Example: Wave 1 can run 4 agents in parallel:**
```
INFRA (done)
├─→ AGENT-DATA (entities) ────┐
├─→ AGENT-NETWORK (capture) ──┼─→ PROTOCOL → BUSINESS → API
├─→ AGENT-AUTOMATION (gui) ───┤
└─→ AGENT-DOCS (README) ──────┘
```

---

## 3. Task Breakdown by Phase

### PHASE 0: Foundation (Week 1) - SEQUENTIAL

**Goal:** Project scaffolding and environment setup

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T0.1** | INFRA | Create Maven/Spring Boot project | 1 day | **BLOCKS ALL** | 🔴 TODO |
| **T0.2** | INFRA | Setup PostgreSQL + Docker Compose | 1 day | BLOCKS DATA | 🔴 TODO |
| **T0.3** | INFRA | Configure application.yml structure | 0.5 day | BLOCKS ALL | 🔴 TODO |
| **T0.4** | INFRA | Setup logging (SLF4J + Logback) | 0.5 day | Non-blocking | 🔴 TODO |
| **T0.5** | INFRA | Create package structure | 0.5 day | BLOCKS ALL | 🔴 TODO |
| **T0.6** | INFRA | Add core dependencies (pom.xml) | 0.5 day | BLOCKS specific agents | 🔴 TODO |
| **T0.7** | INFRA | Setup CI/CD skeleton (GitHub Actions) | 1 day | Non-blocking | 🔴 TODO |

**Output:** Runnable Spring Boot application (empty)
**Validation:** `mvn clean install` succeeds, app starts

---

### PHASE 1: Core Modules (Weeks 2-3) - HIGH PARALLELISM ⚡

**Goal:** Implement independent modules in parallel

#### 🔵 TRACK 1A: Database Layer (AGENT-DATA)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T1.1** | DATA | Create JPA entities (Item, PriceEntry, SubCategory) | 1 day | BLOCKS BUSINESS | 🔴 TODO |
| **T1.2** | DATA | Create repository interfaces | 0.5 day | BLOCKS BUSINESS | 🔴 TODO |
| **T1.3** | DATA | Create Flyway migration scripts | 0.5 day | BLOCKS DATA tests | 🔴 TODO |
| **T1.4** | DATA | Add database indexes and constraints | 0.5 day | Non-blocking | 🔴 TODO |
| **T1.5** | DATA | Create DTOs (ItemDTO, PriceHistoryDTO) | 0.5 day | BLOCKS API | 🔴 TODO |
| **T1.6** | TEST | Write repository unit tests | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** T0.1, T0.2, T0.5 (INFRA complete)
**Output:** Working JPA entities with repositories

---

#### 🔵 TRACK 1B: Network Capture (AGENT-NETWORK)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T1.7** | NETWORK | Research Pcap4j + create PoC | 1 day | BLOCKS T1.8 | 🔴 TODO |
| **T1.8** | NETWORK | Implement PacketCaptureService | 2 days | BLOCKS PROTOCOL | 🔴 TODO |
| **T1.9** | NETWORK | Create packet filter (Dofus Retro port) | 0.5 day | BLOCKS PROTOCOL | 🔴 TODO |
| **T1.10** | NETWORK | Implement packet queue (BlockingQueue) | 0.5 day | BLOCKS BUSINESS | 🔴 TODO |
| **T1.11** | NETWORK | Add graceful start/stop logic | 0.5 day | Non-blocking | 🔴 TODO |
| **T1.12** | TEST | Write packet capture tests (mock pcap) | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** T0.1, T0.5, T0.6 (INFRA + Pcap4j dependency)
**Output:** Service that captures raw packets

---

#### 🔵 TRACK 1C: GUI Automation (AGENT-AUTOMATION)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T1.13** | AUTO | Research Java Robot + Sikuli PoC | 1 day | BLOCKS T1.14 | 🔴 TODO |
| **T1.14** | AUTO | Implement Action interface + state machine | 1 day | BLOCKS T1.15 | 🔴 TODO |
| **T1.15** | AUTO | Create template matching service (JavaCV) | 2 days | BLOCKS T1.16 | 🔴 TODO |
| **T1.16** | AUTO | Implement AuctionHouseAutomationService | 2 days | BLOCKS BUSINESS | 🔴 TODO |
| **T1.17** | AUTO | Create action implementations (click, scroll) | 1 day | BLOCKS testing | 🔴 TODO |
| **T1.18** | AUTO | Add cross-platform coordinate handling | 1 day | Non-blocking | 🔴 TODO |
| **T1.19** | TEST | Write automation tests (headless mode) | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** T0.1, T0.5, T0.6 (INFRA + Sikuli dependency)
**Output:** Service that can automate GUI clicks

---

#### 🔵 TRACK 1D: Protocol Research (AGENT-PROTOCOL)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T1.20** | PROTOCOL | Capture Dofus Retro packets (Wireshark) | 2 days | **CRITICAL** BLOCKS T1.21 | 🔴 TODO |
| **T1.21** | PROTOCOL | Analyze HDV packet structure | 2 days | **CRITICAL** BLOCKS T1.22 | 🔴 TODO |
| **T1.22** | PROTOCOL | Document message IDs and format | 1 day | BLOCKS T1.23 | 🔴 TODO |
| **T1.23** | PROTOCOL | Create MessageDefinition classes | 1 day | BLOCKS T1.24 | 🔴 TODO |
| **T1.24** | PROTOCOL | Implement BinaryReader utility | 1 day | BLOCKS T1.25 | 🔴 TODO |
| **T1.25** | PROTOCOL | Implement DofusRetroProtocolParser | 2 days | BLOCKS BUSINESS | 🔴 TODO |
| **T1.26** | TEST | Write parser tests (sample packets) | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** T1.8, T1.9 (NETWORK capture working)
**Output:** Parser that extracts item/price data from packets
**⚠️ RISK:** This is the critical path and highest risk item

---

#### 🔵 TRACK 1E: Documentation Start (AGENT-DOCS)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T1.27** | DOCS | Update README with Java setup | 0.5 day | Non-blocking | 🔴 TODO |
| **T1.28** | DOCS | Create architecture diagram (Mermaid) | 0.5 day | Non-blocking | 🔴 TODO |
| **T1.29** | DOCS | Document environment setup | 0.5 day | Non-blocking | 🔴 TODO |

**Dependencies:** None (can start anytime)
**Output:** Initial documentation

---

### PHASE 2: Business Logic & Integration (Week 4-5) - MEDIUM PARALLELISM

**Goal:** Connect modules and implement core business logic

#### 🟢 TRACK 2A: Service Layer (AGENT-BUSINESS)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T2.1** | BUSINESS | Implement ItemPriceService | 2 days | BLOCKS API | 🔴 TODO |
| **T2.2** | BUSINESS | Implement deduplication cache (Caffeine) | 1 day | BLOCKS T2.1 | 🔴 TODO |
| **T2.3** | BUSINESS | Implement item enrichment (JSON loader) | 1 day | BLOCKS T2.1 | 🔴 TODO |
| **T2.4** | BUSINESS | Create packet processing pipeline | 2 days | BLOCKS integration | 🔴 TODO |
| **T2.5** | BUSINESS | Implement async queue consumer | 1 day | BLOCKS integration | 🔴 TODO |
| **T2.6** | TEST | Write service layer tests | 2 days | Non-blocking | 🔴 TODO |

**Dependencies:** T1.1-T1.6 (DATA), T1.25 (PROTOCOL), T1.10 (NETWORK queue)
**Output:** Core business logic for processing packets

---

#### 🟢 TRACK 2B: REST API (AGENT-API)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T2.7** | API | Create ItemPriceController | 1 day | BLOCKS docs | 🔴 TODO |
| **T2.8** | API | Implement GET /api/items | 1 day | BLOCKS docs | 🔴 TODO |
| **T2.9** | API | Implement GET /api/items/{id}/prices | 1 day | BLOCKS docs | 🔴 TODO |
| **T2.10** | API | Implement GET /api/categories | 0.5 day | BLOCKS docs | 🔴 TODO |
| **T2.11** | API | Implement GET /api/health | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.12** | API | Add pagination support | 1 day | Non-blocking | 🔴 TODO |
| **T2.13** | API | Add global error handling | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.14** | API | Add CORS configuration | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.15** | TEST | Write controller tests (MockMvc) | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** T2.1 (BUSINESS service)
**Output:** Working REST API

---

#### 🟢 TRACK 2C: Configuration (AGENT-INFRA)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T2.16** | INFRA | Create DofusRetroProperties config class | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.17** | INFRA | Externalize network settings | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.18** | INFRA | Externalize automation settings | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.19** | INFRA | Create application-dev.yml | 0.5 day | Non-blocking | 🔴 TODO |
| **T2.20** | INFRA | Create application-prod.yml | 0.5 day | Non-blocking | 🔴 TODO |

**Dependencies:** All PHASE 1 tasks (knows what to configure)
**Output:** Externalized configuration

---

### PHASE 3: Integration & Testing (Week 6) - LOW PARALLELISM

**Goal:** Connect all modules and validate end-to-end

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T3.1** | INFRA | Wire all services in main application | 1 day | BLOCKS T3.2 | 🔴 TODO |
| **T3.2** | BUSINESS | Implement thread coordination | 1 day | BLOCKS T3.3 | 🔴 TODO |
| **T3.3** | TEST | End-to-end integration test | 2 days | BLOCKS deployment | 🔴 TODO |
| **T3.4** | TEST | Performance testing | 1 day | Non-blocking | 🔴 TODO |
| **T3.5** | TEST | Cross-platform testing (Win/Linux) | 2 days | BLOCKS deployment | 🔴 TODO |
| **T3.6** | DOCS | Create OpenAPI documentation | 1 day | Non-blocking | 🔴 TODO |
| **T3.7** | DOCS | Write user guide | 1 day | Non-blocking | 🔴 TODO |

**Dependencies:** All PHASE 2 tasks
**Output:** Integrated, tested application

---

### PHASE 4: Deployment & Polish (Week 7) - SEQUENTIAL

**Goal:** Production-ready deployment

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T4.1** | INFRA | Create Dockerfile | 0.5 day | BLOCKS T4.2 | 🔴 TODO |
| **T4.2** | INFRA | Update Docker Compose (app + db) | 0.5 day | BLOCKS T4.3 | 🔴 TODO |
| **T4.3** | INFRA | Test Docker deployment | 1 day | BLOCKS release | 🔴 TODO |
| **T4.4** | INFRA | Setup CI/CD pipeline | 1 day | Non-blocking | 🔴 TODO |
| **T4.5** | DOCS | Finalize README | 0.5 day | Non-blocking | 🔴 TODO |
| **T4.6** | DOCS | Create deployment guide | 0.5 day | Non-blocking | 🔴 TODO |
| **T4.7** | TEST | Run full test suite | 0.5 day | BLOCKS release | 🔴 TODO |

**Dependencies:** T3.3, T3.5 (Integration tests pass)
**Output:** Deployable Docker image

---

## 4. Dependency Matrix

### 4.1 Task Dependencies (Directed Graph)

```
Foundation Phase (SEQUENTIAL):
T0.1 → T0.2 → T0.3 → T0.5 → T0.6
       └────────────────┴──────┴────→ [Phase 1 unlocked]

Phase 1 - Parallel Tracks:

TRACK 1A (DATA):
T0.1,T0.2,T0.5 → T1.1 → T1.2 → T1.3
                         └──────┴────→ [T2.1 BUSINESS]
                 T1.5 ──────────────→ [T2.7 API]

TRACK 1B (NETWORK):
T0.1,T0.5,T0.6 → T1.7 → T1.8 → T1.9 → T1.10
                                └──────┴────→ [T1.20 PROTOCOL]
                         T1.10 ──────────────→ [T2.4 BUSINESS]

TRACK 1C (AUTOMATION):
T0.1,T0.5,T0.6 → T1.13 → T1.14 → T1.15 → T1.16
                                          └───→ [T2.4 BUSINESS]

TRACK 1D (PROTOCOL) - CRITICAL PATH:
T1.8,T1.9 → T1.20 → T1.21 → T1.22 → T1.23 → T1.24 → T1.25
                                                      └────→ [T2.1 BUSINESS]

Phase 2 - Moderate Parallelism:

TRACK 2A (BUSINESS):
T1.2,T1.25,T1.10 → T2.2,T2.3 → T2.1 → T2.4,T2.5
                                       └──────┴───→ [T2.7 API]

TRACK 2B (API):
T2.1,T1.5 → T2.7 → T2.8,T2.9,T2.10 → [T3.3 Integration]

Phase 3 - Integration (MOSTLY SEQUENTIAL):
All Phase 2 → T3.1 → T3.2 → T3.3,T3.5 → [Phase 4]

Phase 4 - Deployment (SEQUENTIAL):
T3.3,T3.5 → T4.1 → T4.2 → T4.3 → T4.7 → [RELEASE]
```

### 4.2 Blocking Analysis

**🔴 CRITICAL BLOCKERS (longest chain):**
```
T0.1 (Infra) → T1.8 (Network) → T1.20-21 (Protocol) → T2.1 (Business) → T2.7 (API) → T3.3 (Test)
```
**Duration:** 1d + 2d + 4d + 2d + 1d + 2d = 12 days (critical path)

**🟡 PARALLEL OPPORTUNITIES:**
- Phase 1: DATA, AUTOMATION, DOCS can run 100% parallel to NETWORK
- Phase 1: T1.6, T1.12, T1.19, T1.26 (tests) can run parallel to later tasks
- Phase 2: T2.16-T2.20 (config) parallel to API development
- Phase 3: T3.4, T3.6, T3.7 parallel to core integration

**🟢 NON-BLOCKING (can be deferred):**
- T0.4 (Logging)
- T0.7 (CI/CD skeleton)
- All TEST tasks (except T3.3)
- All DOCS tasks

---

## 5. Implementation Roadmap

### 5.1 Week-by-Week Plan

#### Week 1: Foundation 🏗️
**Active Agents:** AGENT-INFRA (solo)
**Parallel Capacity:** 1/9 agents = 11% utilization

```
Day 1-2: T0.1, T0.2, T0.3
Day 3: T0.5, T0.6
Day 4-5: T0.7 (CI/CD)
```

**Deliverable:** Empty Spring Boot app with PostgreSQL

---

#### Week 2-3: Core Modules ⚡
**Active Agents:** AGENT-DATA, AGENT-NETWORK, AGENT-AUTOMATION, AGENT-PROTOCOL, AGENT-DOCS
**Parallel Capacity:** 5/9 agents = 56% utilization

**Week 2:**
```
AGENT-DATA:      [T1.1][T1.2][T1.3][T1.4][T1.5]
AGENT-NETWORK:   [T1.7][T1.8──────────][T1.9][T1.10]
AGENT-AUTOMATION:[T1.13][T1.14][T1.15─────────]
AGENT-PROTOCOL:  [wait][T1.20─────────][T1.21─────]
AGENT-DOCS:      [T1.27][T1.28][T1.29]
```

**Week 3:**
```
AGENT-DATA:      [T1.6 tests]
AGENT-NETWORK:   [T1.11][T1.12 tests]
AGENT-AUTOMATION:[T1.16─────────][T1.17][T1.18]
AGENT-PROTOCOL:  [T1.22][T1.23][T1.24][T1.25────]
AGENT-TEST:      [Start test coverage]
```

**Deliverable:** All core modules implemented independently

---

#### Week 4-5: Integration 🔗
**Active Agents:** AGENT-BUSINESS, AGENT-API, AGENT-INFRA, AGENT-TEST
**Parallel Capacity:** 4/9 agents = 44% utilization

**Week 4:**
```
AGENT-BUSINESS:  [T2.2][T2.3][T2.1──────][T2.4──────]
AGENT-API:       [wait────────][T2.7][T2.8][T2.9]
AGENT-INFRA:     [T2.16][T2.17][T2.18][T2.19][T2.20]
AGENT-TEST:      [T2.6 service tests]
```

**Week 5:**
```
AGENT-BUSINESS:  [T2.5]
AGENT-API:       [T2.10][T2.11][T2.12][T2.13][T2.14]
AGENT-TEST:      [T2.15 controller tests]
AGENT-DOCS:      [Update documentation]
```

**Deliverable:** Integrated application with REST API

---

#### Week 6: Testing 🧪
**Active Agents:** AGENT-TEST, AGENT-INFRA, AGENT-DOCS
**Parallel Capacity:** 3/9 agents = 33% utilization

```
AGENT-TEST:      [T3.3──────][T3.4──][T3.5────────]
AGENT-INFRA:     [T3.1][T3.2]
AGENT-DOCS:      [T3.6][T3.7]
```

**Deliverable:** Fully tested application

---

#### Week 7: Deployment 🚀
**Active Agents:** AGENT-INFRA, AGENT-DOCS, AGENT-TEST
**Parallel Capacity:** 3/9 agents = 33% utilization

```
AGENT-INFRA:     [T4.1][T4.2][T4.3][T4.4]
AGENT-DOCS:      [T4.5][T4.6]
AGENT-TEST:      [T4.7]
```

**Deliverable:** Production-ready Docker image

---

### 5.2 Parallel Execution Gantt Chart

```
Week:       1        2        3        4        5        6        7
─────────────────────────────────────────────────────────────────────
INFRA:    ████████─────────────────────░░░░░░░░░░░░░░░░░░████████████
DATA:     ────────████████████─────────────────────────────────────────
NETWORK:  ────────████████████████─────────────────────────────────────
PROTOCOL: ────────────────████████████████────────────────────────────
AUTOMATION:───────████████████████────────────────────────────────────
BUSINESS: ─────────────────────────────████████████─────────────────────
API:      ─────────────────────────────────────████████────────────────
TEST:     ────────────────░░░░░░░░░░░░░░░░░░░░░░████████████░░░░░░────
DOCS:     ────────░░░░────────────────────────────────────░░░░████────

Legend:
████ = Active work (blocking or critical)
░░░░ = Background work (non-blocking)
──── = Idle
```

---

## 6. Task Catalog

### 6.1 Task Template

Each task follows this structure:

```markdown
### Task ID: TXXX
**Agent:** AGENT-NAME
**Phase:** X
**Track:** XY
**Duration:** X days
**Priority:** 🔴 Critical / 🟡 High / 🟢 Medium / ⚪ Low

**Description:**
Clear description of what to build/implement

**Dependencies:**
- Task ID: Why this is needed
- Task ID: What input is required

**Blocks:**
- Task ID: What depends on this

**Acceptance Criteria:**
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Tests pass

**Inputs:**
- File/artifact from previous task

**Outputs:**
- File/artifact produced

**Implementation Notes:**
- Key technical details
- Code references
- Gotchas
```

---

### 6.2 Sample Task: T1.1 (Create JPA Entities)

---

### Task ID: T1.1
**Agent:** AGENT-DATA
**Phase:** 1
**Track:** 1A
**Duration:** 1 day
**Priority:** 🔴 Critical

**Description:**
Create JPA entity classes for Item, PriceEntry, and SubCategory based on the database schema defined in the PRD (Section 6.2).

**Dependencies:**
- T0.1: Project structure exists
- T0.2: PostgreSQL configured
- T0.5: Package structure created

**Blocks:**
- T1.2: Repository interfaces (needs entities)
- T2.1: ItemPriceService (needs entities)

**Acceptance Criteria:**
- [ ] `Item.java` entity created with all fields
- [ ] `PriceEntry.java` entity created with relationships
- [ ] `SubCategory.java` entity created
- [ ] Proper JPA annotations (@Entity, @Table, @Column)
- [ ] Bidirectional relationships configured
- [ ] Lombok annotations added (@Data, @NoArgsConstructor)
- [ ] Entities compile without errors
- [ ] Application starts without JPA errors

**Inputs:**
- PRD Section 6.2 (Entity definitions)
- `src/main/java/com/dofusretro/pricetracker/model/` package

**Outputs:**
- `model/Item.java`
- `model/PriceEntry.java`
- `model/SubCategory.java`

**Implementation Notes:**
```java
// Key points:
// 1. Use @Table(indexes = ...) for performance
// 2. Use FetchType.LAZY for relationships
// 3. Add @CreationTimestamp and @UpdateTimestamp
// 4. Use Long for IDs (not Integer)
// 5. Ensure equals/hashCode only use business keys

// Example structure:
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_gid", columnList = "item_gid")
})
@Data
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_gid", nullable = false)
    private Integer itemGid;

    // ... rest of fields
}
```

**Testing:**
- Create `ItemEntityTest.java` with basic persistence tests
- Use `@DataJpaTest` annotation

---

### 6.3 All Tasks Summary

Total tasks: **73**
Critical path tasks: **18**
Parallelizable tasks: **42**
Non-blocking tasks: **13**

**By Agent:**
- AGENT-INFRA: 15 tasks
- AGENT-DATA: 6 tasks
- AGENT-NETWORK: 6 tasks
- AGENT-PROTOCOL: 7 tasks
- AGENT-AUTOMATION: 7 tasks
- AGENT-BUSINESS: 6 tasks
- AGENT-API: 9 tasks
- AGENT-TEST: 10 tasks
- AGENT-DOCS: 7 tasks

---

## 7. Coordination Protocol

### 7.1 Agent Communication Rules

**Interface Contracts:**
When agents need to work in parallel but have dependencies, create **interface contracts** first:

```java
// AGENT-PROTOCOL creates interface
public interface ProtocolParser {
    ParsedMessage parse(byte[] rawPacket);
}

// AGENT-BUSINESS can develop against interface immediately
// AGENT-PROTOCOL implements later
```

**Handoff Points:**
```
AGENT-A completes → commits code → tags commit → notifies AGENT-B
                                                   ↓
                                        AGENT-B pulls, validates, continues
```

---

### 7.2 Integration Checkpoints

**Checkpoint 1: End of Phase 1 (Week 3)**
```
Required Outputs:
✓ All entities created (DATA)
✓ PacketCaptureService captures packets (NETWORK)
✓ Protocol parser parses test packets (PROTOCOL)
✓ Automation can click buttons (AUTOMATION)

Integration Test:
- Create in-memory packet → Parse → Store in H2 → Query via repository
- Validate end-to-end without GUI
```

**Checkpoint 2: End of Phase 2 (Week 5)**
```
Required Outputs:
✓ ItemPriceService processes packets (BUSINESS)
✓ REST API returns data (API)
✓ Full pipeline: Packet → Parse → Process → Store → API → JSON

Integration Test:
- Inject test packet → Verify appears in API response
- Test with 100 packets → Verify deduplication works
```

**Checkpoint 3: End of Phase 3 (Week 6)**
```
Required Outputs:
✓ Full application runs end-to-end
✓ GUI automation triggers real packets
✓ Data appears in database
✓ API serves real data

Integration Test:
- Start Dofus Retro → Run app → Verify prices collected
- Check database for entries
- Query API for collected items
```

---

### 7.3 Conflict Resolution

**Code Conflicts:**
- Each agent works in separate package/module
- Minimize shared files
- Use feature branches: `feature/T1.1-jpa-entities`

**Design Conflicts:**
- Refer to PRD as source of truth
- Escalate to architecture review
- Document decisions in ADR (Architecture Decision Record)

**Dependency Conflicts:**
- Maven handles version resolution
- Document version choices in `pom.xml` comments
- Test integration regularly

---

## 8. Integration Points

### 8.1 Module Integration Map

```
┌─────────────────────────────────────────────────────────┐
│                    Application Main                     │
│                   (AGENT-INFRA T3.1)                    │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ↓               ↓               ↓
┌─────────────────┐ ┌─────────────┐ ┌──────────────────┐
│ PacketCapture   │ │   GUI       │ │  REST API        │
│ Service         │ │ Automation  │ │  Controller      │
│ (NETWORK)       │ │ (AUTO)      │ │  (API)           │
└────────┬────────┘ └──────────── ─┘ └────────┬─────────┘
         │                                     │
         │ BlockingQueue                       │
         ↓                                     ↓
┌─────────────────────────────────────────────────────────┐
│              ItemPriceService                           │
│              (BUSINESS)                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Protocol     │  │ Cache        │  │ Enrichment   │ │
│  │ Parser       │  │ (Caffeine)   │  │ (JSON)       │ │
│  │ (PROTOCOL)   │  │              │  │              │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│              Spring Data JPA                            │
│              (DATA)                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ Item         │  │ PriceEntry   │  │ SubCategory  │ │
│  │ Repository   │  │ Repository   │  │ Repository   │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└────────────────────────┬────────────────────────────────┘
                         ↓
                  [ PostgreSQL ]
```

### 8.2 Data Flow Integration

**Flow 1: Packet Capture → Database**
```
[Dofus Retro] → [Pcap4j] → BlockingQueue<byte[]>
                                 ↓
                         ProtocolParser.parse()
                                 ↓
                         BlockingQueue<PacketData>
                                 ↓
                      ItemPriceService.processPacket()
                                 ↓
                         [Check cache]
                                 ↓
                         [Enrich item name]
                                 ↓
                         ItemRepository.save()
                                 ↓
                           [PostgreSQL]
```

**Flow 2: API Query**
```
[HTTP GET /api/items/1/prices] → Controller
                                      ↓
                              ItemPriceService.getPriceHistory()
                                      ↓
                              PriceEntryRepository.findByItem()
                                      ↓
                              [Map to DTO]
                                      ↓
                              [Return JSON]
```

---

## 9. Success Validation

### 9.1 Phase Completion Criteria

**Phase 0 Complete:**
```bash
✓ mvn clean install (success)
✓ mvn spring-boot:run (app starts)
✓ curl http://localhost:8080/actuator/health (returns UP)
✓ psql -h localhost -U dofus (connects to database)
```

**Phase 1 Complete:**
```bash
✓ All unit tests pass (mvn test)
✓ Code coverage >60% (jacoco:report)
✓ PacketCaptureService captures test pcap
✓ ProtocolParser parses sample packet
✓ AuctionHouseAutomation clicks button
✓ Repositories save/retrieve entities
```

**Phase 2 Complete:**
```bash
✓ All integration tests pass
✓ curl http://localhost:8080/api/items (returns JSON)
✓ curl http://localhost:8080/api/items/1/prices (returns data)
✓ Packet → Parse → Save → Query (full pipeline works)
✓ Deduplication prevents duplicates
```

**Phase 3 Complete:**
```bash
✓ E2E test: Start app → Collects data → Query API
✓ Cross-platform test (Windows + Linux)
✓ Performance: <25 minutes full collection
✓ Code coverage >80%
✓ No critical SonarQube issues
```

**Phase 4 Complete:**
```bash
✓ docker-compose up (app starts)
✓ Docker container collects data
✓ API accessible from host machine
✓ CI/CD pipeline runs successfully
✓ README has complete instructions
```

---

### 9.2 Agent Deliverable Checklist

Each agent must provide:

**Code Deliverables:**
- [ ] Source code in correct package
- [ ] Unit tests with >80% coverage
- [ ] JavaDoc for public classes/methods
- [ ] No compiler warnings
- [ ] Follows Google Java Style Guide

**Documentation Deliverables:**
- [ ] Inline code comments for complex logic
- [ ] README section (if applicable)
- [ ] Architecture diagram update (if applicable)

**Testing Deliverables:**
- [ ] Unit tests (JUnit 5)
- [ ] Integration tests (if integration point)
- [ ] Test data builders/fixtures
- [ ] Test documentation

**Integration Deliverables:**
- [ ] Interface contracts defined
- [ ] Configuration properties documented
- [ ] Dependencies declared in pom.xml
- [ ] Spring beans properly annotated

---

## 10. Quick Start Guide for Agents

### 10.1 How to Pick Up a Task

**Step 1: Check Dependencies**
```bash
# Review task TXXX in this document
# Verify all dependencies completed
# Check for "BLOCKS" warnings
```

**Step 2: Setup Local Environment**
```bash
git checkout -b feature/TXXX-short-description
mvn clean install
# Verify builds successfully
```

**Step 3: Implement**
```bash
# Follow task acceptance criteria
# Write tests first (TDD)
# Implement functionality
# Run tests: mvn test
```

**Step 4: Validate**
```bash
# Check acceptance criteria (all ✓)
# Run full build: mvn clean verify
# Run integration tests (if applicable)
# Update task status in this document
```

**Step 5: Handoff**
```bash
git add .
git commit -m "[TXXX] Short description"
git push origin feature/TXXX
# Create PR, link to task
# Notify dependent agents
```

---

### 10.2 Agent Priority Queue

**Current Sprint: Phase 0 (Week 1)**
```
Active: AGENT-INFRA
Queue:  T0.1 → T0.2 → T0.3 → T0.5 → T0.6

Next Up: AGENT-DATA (waiting for T0.1, T0.2, T0.5)
```

**Next Sprint: Phase 1 (Week 2-3)**
```
Parallel Launch:
- AGENT-DATA → T1.1 (HIGH PRIORITY)
- AGENT-NETWORK → T1.7 (HIGH PRIORITY)
- AGENT-AUTOMATION → T1.13 (MEDIUM PRIORITY)
- AGENT-DOCS → T1.27 (LOW PRIORITY)

Critical Path: AGENT-PROTOCOL → T1.20 (CRITICAL - needs T1.8 first)
```

---

## 11. Risk Mitigation

### 11.1 Critical Risks

**Risk 1: Protocol Reverse Engineering Takes >2 Weeks**
- **Impact:** Blocks entire project
- **Mitigation:**
  - Start T1.20 ASAP (even before NETWORK complete)
  - Use Wireshark captures from Python version
  - Allocate AGENT-PROTOCOL full-time
  - Create mock parser for parallel development
- **Fallback:** Use partial protocol, implement more later

**Risk 2: Pcap4j Requires Admin/Root**
- **Impact:** Deployment complexity
- **Mitigation:**
  - Test early on all platforms
  - Document privilege requirements
  - Create Docker with capabilities
- **Fallback:** Use tcpdump file replay

**Risk 3: Sikuli Performance Issues**
- **Impact:** GUI automation slow
- **Mitigation:**
  - Benchmark early
  - Optimize template matching
  - Use region-based matching
- **Fallback:** Pure Java Robot (faster but less reliable)

---

## 12. Appendix

### 12.1 Glossary

- **Blocking Task:** Must complete before dependent tasks start
- **Critical Path:** Longest dependency chain (determines minimum duration)
- **Parallel Track:** Independent task sequences that can run simultaneously
- **Integration Point:** Where multiple modules connect
- **Handoff:** Transfer of completed work between agents

### 12.2 References

- Main PRD: `DOFUS_RETRO_PRD.md`
- Python Source: `/home/user/HDVParserDofus2Python/`
- Task Tracking: This document (IMPLEMENTATION_BOOK.md)

---

## 13. Task Status Dashboard

**Legend:**
- 🔴 TODO
- 🟡 IN PROGRESS
- 🟢 DONE
- ⚪ BLOCKED

### Phase 0: Foundation
- T0.1 🔴 - T0.2 🔴 - T0.3 🔴 - T0.4 🔴 - T0.5 🔴 - T0.6 🔴 - T0.7 🔴

### Phase 1: Core Modules
**Track 1A (DATA):**
- T1.1 🔴 - T1.2 🔴 - T1.3 🔴 - T1.4 🔴 - T1.5 🔴 - T1.6 🔴

**Track 1B (NETWORK):**
- T1.7 🔴 - T1.8 🔴 - T1.9 🔴 - T1.10 🔴 - T1.11 🔴 - T1.12 🔴

**Track 1C (AUTOMATION):**
- T1.13 🔴 - T1.14 🔴 - T1.15 🔴 - T1.16 🔴 - T1.17 🔴 - T1.18 🔴 - T1.19 🔴

**Track 1D (PROTOCOL) - CRITICAL:**
- T1.20 🔴 - T1.21 🔴 - T1.22 🔴 - T1.23 🔴 - T1.24 🔴 - T1.25 🔴 - T1.26 🔴

**Track 1E (DOCS):**
- T1.27 🔴 - T1.28 🔴 - T1.29 🔴

### Phase 2: Integration
**Track 2A (BUSINESS):**
- T2.1 🔴 - T2.2 🔴 - T2.3 🔴 - T2.4 🔴 - T2.5 🔴 - T2.6 🔴

**Track 2B (API):**
- T2.7 🔴 - T2.8 🔴 - T2.9 🔴 - T2.10 🔴 - T2.11 🔴 - T2.12 🔴 - T2.13 🔴 - T2.14 🔴 - T2.15 🔴

**Track 2C (CONFIG):**
- T2.16 🔴 - T2.17 🔴 - T2.18 🔴 - T2.19 🔴 - T2.20 🔴

### Phase 3: Testing
- T3.1 🔴 - T3.2 🔴 - T3.3 🔴 - T3.4 🔴 - T3.5 🔴 - T3.6 🔴 - T3.7 🔴

### Phase 4: Deployment
- T4.1 🔴 - T4.2 🔴 - T4.3 🔴 - T4.4 🔴 - T4.5 🔴 - T4.6 🔴 - T4.7 🔴

---

**Total Progress: 0/73 tasks complete (0%)**

---

## END OF IMPLEMENTATION BOOK

**Last Updated:** 2025-11-08
**Next Review:** After Phase 0 completion

---

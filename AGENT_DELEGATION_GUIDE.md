# Agent Delegation Guide
# How to Execute the Dofus Retro Project with Multiple Agents

**Version:** 1.0
**Date:** 2025-11-08
**Purpose:** Practical guide for delegating tasks to multiple AI agents in parallel

---

## 📋 Table of Contents

1. [Quick Start](#1-quick-start)
2. [Agent Prompt Templates](#2-agent-prompt-templates)
3. [Phase-by-Phase Execution](#3-phase-by-phase-execution)
4. [Parallel Launch Strategy](#4-parallel-launch-strategy)
5. [Integration & Handoffs](#5-integration--handoffs)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Quick Start

### 1.1 Prerequisites

Before launching agents, ensure:
- [ ] PRD (`DOFUS_RETRO_PRD.md`) is complete and approved
- [ ] Implementation Book (`IMPLEMENTATION_BOOK.md`) is available
- [ ] Git repository is set up
- [ ] You have a task tracking system (GitHub Projects, Jira, or spreadsheet)

### 1.2 Execution Overview

```
Step 1: Launch AGENT-INFRA (solo) → Foundation (Week 1)
Step 2: Launch 5 agents in parallel → Core Modules (Week 2-3)
Step 3: Launch 3 agents in parallel → Integration (Week 4-5)
Step 4: Launch 3 agents in parallel → Testing (Week 6)
Step 5: Launch 2 agents in parallel → Deployment (Week 7)
```

### 1.3 Agent Launch Checklist

For each agent launch:
- [ ] Read task details from Implementation Book
- [ ] Verify dependencies are complete
- [ ] Create feature branch
- [ ] Provide agent with clear scope
- [ ] Monitor progress
- [ ] Validate deliverables
- [ ] Merge and notify dependent agents

---

## 2. Agent Prompt Templates

### 2.1 General Agent Prompt Structure

```markdown
You are **AGENT-{NAME}**, a specialized AI assistant working on the Dofus Retro Price Tracker project.

**Your Role:** {role description from Implementation Book Section 1.1}

**Your Mission:** Complete task **T{X.Y}** from the Implementation Book.

**Context:**
- Read: DOFUS_RETRO_PRD.md (Section {relevant sections})
- Read: IMPLEMENTATION_BOOK.md (Task T{X.Y})
- Reference: {any existing code/files}

**Task Details:**
{Copy task details from Implementation Book}

**Acceptance Criteria:**
{Copy acceptance criteria from task}

**Instructions:**
1. Create feature branch: `feature/T{X.Y}-{short-description}`
2. Implement according to acceptance criteria
3. Write unit tests (>80% coverage)
4. Add JavaDoc for public APIs
5. Validate all criteria are met
6. Commit with message: "[T{X.Y}] {description}"
7. Report completion with summary

**Constraints:**
- Stay within your scope (only T{X.Y})
- Follow Spring Boot best practices
- Use Java 26 features where appropriate
- No external dependencies without approval

**Deliverables:**
- Source code
- Unit tests
- Documentation (JavaDoc + comments)
- Summary report

Start now!
```

---

### 2.2 Phase 0: AGENT-INFRA Template

**Launch Command:**

```markdown
You are **AGENT-INFRA**, the Infrastructure & Setup Specialist.

**Mission:** Complete Phase 0 (Foundation) - Tasks T0.1 through T0.7

**Context:**
- Project: Dofus Retro Price Tracker (Java 26 + Spring Boot)
- Read: DOFUS_RETRO_PRD.md (Section 5: Technical Architecture, Section 7: Technology Stack)
- Read: IMPLEMENTATION_BOOK.md (Phase 0 tasks)

**Your Tasks (execute sequentially):**

### T0.1: Create Maven/Spring Boot Project
- Initialize Spring Boot 3.3 project with Maven
- Group: com.dofusretro
- Artifact: price-tracker
- Package: jar
- Java: 26
- Dependencies: web, data-jpa, postgresql, lombok, actuator

### T0.2: Setup PostgreSQL + Docker Compose
- Create docker-compose.yml with PostgreSQL 16
- Database name: dofus_retro_db
- Create application.yml with datasource config

### T0.3: Configure application.yml Structure
- Create profiles: dev, prod
- Setup logging configuration
- Configure JPA properties

### T0.4: Setup Logging (SLF4J + Logback)
- Create logback-spring.xml
- Console + file appenders
- JSON format for structured logging

### T0.5: Create Package Structure
- model/ (entities)
- repository/ (Spring Data JPA)
- service/ (business logic)
- controller/ (REST API)
- config/ (Spring configuration)
- protocol/ (packet parsing)
- automation/ (GUI automation)
- dto/ (data transfer objects)

### T0.6: Add Core Dependencies
- Add Pcap4j (packet capture)
- Add Caffeine (caching)
- Add Flyway (database migrations)
- Add JavaCV (image processing)
- Add JUnit 5, Mockito, AssertJ (testing)

### T0.7: Setup CI/CD Skeleton
- Create .github/workflows/ci.yml
- Maven build + test on push
- Code coverage with JaCoCo

**Validation:**
Run these commands and verify success:
```bash
mvn clean install
mvn spring-boot:run
curl http://localhost:8080/actuator/health
docker-compose up -d
psql -h localhost -U dofus -d dofus_retro_db -c "SELECT 1"
```

**Deliverables:**
- Complete Maven project structure
- docker-compose.yml
- application.yml (dev + prod)
- pom.xml with all dependencies
- README.md (setup instructions)
- CI/CD workflow

**Timeline:** 5 days

Start now! Report back when T0.1-T0.7 are complete.
```

---

### 2.3 Phase 1: Parallel Agent Launch

#### 2.3.1 AGENT-DATA (Track 1A)

```markdown
You are **AGENT-DATA**, the Database & ORM Specialist.

**Mission:** Implement JPA entities and repositories (Tasks T1.1 - T1.6)

**Context:**
- Foundation (Phase 0) is complete
- Read: DOFUS_RETRO_PRD.md (Section 6: Data Models & Database Design)
- Read: IMPLEMENTATION_BOOK.md (Track 1A tasks)
- Branch: `feature/T1-data-layer`

**Your Tasks:**

### T1.1: Create JPA Entities (1 day)
Create these entities in `model/` package:
- `Item.java` (id, itemGid, itemName, subCategory, prices, timestamps)
- `PriceEntry.java` (id, item, price, quantity, createdAt, serverTimestamp)
- `SubCategory.java` (id, dofusId, name, items)

**Requirements:**
- Use JPA annotations (@Entity, @Table, @Column)
- Add indexes: @Index(name = "idx_item_gid", columnList = "item_gid")
- Use Lombok: @Data, @NoArgsConstructor, @AllArgsConstructor
- Bidirectional relationships (Item ↔ PriceEntry, SubCategory ↔ Item)
- Use FetchType.LAZY for collections

### T1.2: Create Repository Interfaces (0.5 day)
Create in `repository/` package:
- `ItemRepository extends JpaRepository<Item, Long>`
  - `Optional<Item> findByItemGid(Integer itemGid)`
  - `List<Item> findBySubCategory(SubCategory category)`
- `PriceEntryRepository extends JpaRepository<PriceEntry, Long>`
  - `List<PriceEntry> findByItemAndCreatedAtAfter(Item, LocalDateTime)`
  - Custom query for price history
- `SubCategoryRepository extends JpaRepository<SubCategory, Long>`
  - `Optional<SubCategory> findByDofusId(Integer dofusId)`

### T1.3: Create Flyway Migration Scripts (0.5 day)
Create in `src/main/resources/db/migration/`:
- `V1__create_tables.sql` (CREATE TABLE for all entities)
- `V2__add_indexes.sql` (CREATE INDEX for performance)

### T1.4: Add Database Indexes (0.5 day)
Optimize queries:
- idx_item_gid (items.item_gid)
- idx_created_at (price_entries.created_at)
- idx_item_quantity (price_entries.item_id, quantity)

### T1.5: Create DTOs (0.5 day)
Create in `dto/` package:
- `ItemDTO` (for API responses)
- `PriceHistoryDTO` (time series data)
- `CategoryDTO` (category list)
- Use records (Java 16+) or @Data classes

### T1.6: Write Repository Tests (1 day)
Create test classes in `src/test/java/`:
- `ItemRepositoryTest` (@DataJpaTest)
- `PriceEntryRepositoryTest`
- Test save, findById, custom queries
- Use H2 in-memory database for tests

**Validation:**
```bash
mvn test -Dtest=*RepositoryTest
mvn spring-boot:run
# Check logs: "JPA repositories initialized"
```

**Deliverables:**
- 3 entity classes with proper annotations
- 3 repository interfaces
- 2 Flyway migration scripts
- 3 DTO classes
- 3 test classes with >80% coverage
- Documentation (JavaDoc)

**Timeline:** 4 days

**Blocks:** T2.1 (AGENT-BUSINESS needs these entities)

Start now!
```

---

#### 2.3.2 AGENT-NETWORK (Track 1B)

```markdown
You are **AGENT-NETWORK**, the Network & Packet Capture Specialist.

**Mission:** Implement packet capture using Pcap4j (Tasks T1.7 - T1.12)

**Context:**
- Foundation (Phase 0) is complete
- Read: DOFUS_RETRO_PRD.md (Section 2.1: MITM Module)
- Read: IMPLEMENTATION_BOOK.md (Track 1B tasks)
- Branch: `feature/T1-network-capture`

**Your Tasks:**

### T1.7: Research Pcap4j + Create PoC (1 day)
- Study Pcap4j documentation
- Create standalone PoC: capture packets on any port
- Test on Windows + Linux
- Document required permissions (root/admin)

### T1.8: Implement PacketCaptureService (2 days)
Create `service/PacketCaptureService.java`:
```java
@Service
@Slf4j
public class PacketCaptureService {
    private PcapHandle handle;
    private final BlockingQueue<byte[]> packetQueue;

    @PostConstruct
    public void startCapture() {
        // Initialize pcap handle
        // Set BPF filter for Dofus Retro port
        // Start packet listener thread
    }

    @PreDestroy
    public void stopCapture() {
        // Graceful shutdown
    }
}
```

**Requirements:**
- Auto-detect network interface
- Configurable port (via application.yml)
- BPF filter: `tcp port 5555`
- Thread-safe packet queue
- Error handling + retry logic

### T1.9: Create Packet Filter (0.5 day)
- Implement BPF filter for Dofus traffic
- Filter by destination port
- Extract TCP payload only

### T1.10: Implement Packet Queue (0.5 day)
- Use `BlockingQueue<byte[]>` (LinkedBlockingQueue)
- Configurable capacity (default 1000)
- Add metrics (queue size, packets captured)

### T1.11: Add Graceful Start/Stop (0.5 day)
- Spring lifecycle hooks (@PostConstruct, @PreDestroy)
- Handle Ctrl+C gracefully
- Close pcap handle properly

### T1.12: Write Packet Capture Tests (1 day)
- Use test pcap files
- Mock PcapHandle
- Test filter logic
- Test queue overflow handling

**Validation:**
```bash
# Test with local network traffic
mvn spring-boot:run
# Check logs: "PacketCaptureService started on port 5555"
# Generate test traffic → verify queue receives packets
```

**Deliverables:**
- PacketCaptureService.java
- Configuration properties (@ConfigurationProperties)
- Test pcap files in src/test/resources/
- Unit tests (PacketCaptureServiceTest)
- README section: "Network Capture Setup"

**Timeline:** 5 days

**Blocks:** T1.20 (AGENT-PROTOCOL needs packet structure)

Start now!
```

---

#### 2.3.3 AGENT-AUTOMATION (Track 1C)

```markdown
You are **AGENT-AUTOMATION**, the GUI Automation Specialist.

**Mission:** Implement GUI automation using Java Robot + Sikuli (Tasks T1.13 - T1.19)

**Context:**
- Foundation (Phase 0) is complete
- Read: DOFUS_RETRO_PRD.md (Section 2.1: Pixel Module)
- Read: Python implementation: pixel/actions.py, pixel/PixelClicker.py
- Branch: `feature/T1-gui-automation`

**Your Tasks:**

### T1.13: Research Java Robot + Sikuli PoC (1 day)
- Study java.awt.Robot API
- Test Sikuli template matching
- Create PoC: detect button, click it
- Test on Windows + Linux

### T1.14: Implement Action Interface + State Machine (1 day)
Create in `automation/` package:
```java
public interface Action {
    ActionResult execute(Robot robot);
    Action nextAction(ActionResult result);
}

public class ActionStateMachine {
    private Action currentAction;

    public void transition(ActionResult result) {
        currentAction = currentAction.nextAction(result);
    }
}
```

**Requirements:**
- State pattern for actions
- Actions: ClickCategory, ScrollItems, ClickItem, Wait
- Result types: SUCCESS, FAILURE, CATEGORY_END, DONE

### T1.15: Create Template Matching Service (2 days)
Use JavaCV (OpenCV wrapper):
```java
@Service
public class TemplateMatchingService {
    public Point findTemplate(BufferedImage screen,
                               BufferedImage template,
                               double threshold) {
        // OpenCV matchTemplate
        // Return center point of match
    }
}
```

Store templates in `src/main/resources/templates/`:
- `category-unchecked.png`
- `item-slot.png`
- `scroll-bar.png`

### T1.16: Implement AuctionHouseAutomationService (2 days)
```java
@Service
public class AuctionHouseAutomationService {
    private final Robot robot;
    private final ActionStateMachine stateMachine;

    @Scheduled(fixedDelay = 100)
    public void executeNextAction() {
        Action action = stateMachine.getCurrentAction();
        ActionResult result = action.execute(robot);
        stateMachine.transition(result);
    }
}
```

### T1.17: Create Action Implementations (1 day)
- `ClickCategoryAction` (find + click category)
- `ScrollItemsAction` (scroll list)
- `ClickItemAction` (click item to load prices)
- `WaitAction` (delay for rendering)

### T1.18: Add Cross-Platform Coordinate Handling (1 day)
- Detect OS + screen resolution
- Normalize coordinates (Retina vs standard)
- Platform-specific configurations

### T1.19: Write Automation Tests (1 day)
- Mock Robot for testing
- Test state machine transitions
- Test template matching accuracy

**Validation:**
```bash
mvn spring-boot:run
# With Dofus Retro open, automation should:
# 1. Detect HDV window
# 2. Click first category
# 3. Scroll items
# 4. Click item (observe packet capture)
```

**Deliverables:**
- Action interface + implementations (5 classes)
- ActionStateMachine.java
- TemplateMatchingService.java
- AuctionHouseAutomationService.java
- Template images (src/main/resources/templates/)
- Test classes
- README section: "GUI Automation Setup"

**Timeline:** 8 days

**Blocks:** T2.4 (AGENT-BUSINESS needs automation trigger)

Start now!
```

---

#### 2.3.4 AGENT-PROTOCOL (Track 1D) - CRITICAL PATH

```markdown
You are **AGENT-PROTOCOL**, the Protocol Parsing Specialist.

**Mission:** Reverse-engineer Dofus Retro protocol and implement parser (Tasks T1.20 - T1.26)

⚠️ **CRITICAL:** This is on the critical path. Any delay blocks the entire project.

**Context:**
- AGENT-NETWORK has completed packet capture (T1.8)
- Read: DOFUS_RETRO_PRD.md (Section 9.2: Protocol Reverse Engineering)
- Read: Python LaBot framework: labot/protocol.py, labot/data/binrw.py
- Branch: `feature/T1-protocol-parser`

**Your Tasks:**

### T1.20: Capture Dofus Retro Packets (2 days) - CRITICAL
**Setup:**
1. Install Dofus Retro client
2. Install Wireshark
3. Start packet capture: `tcp port 5555`

**Capture These Scenarios:**
- Login sequence
- Opening auction house (HDV)
- Clicking on category (resources)
- Clicking on specific item
- Viewing prices (quantity 1, 10, 100)

**Deliverable:**
- `dofus-retro-hdv.pcap` file
- Document with packet sequence notes
- Screenshots of Wireshark analysis

### T1.21: Analyze HDV Packet Structure (2 days) - CRITICAL
**Analysis Tasks:**
1. Identify HDV message IDs
2. Document message header format
3. Map packet payload structure
4. Identify variable-length encoding (VarInt)
5. Find price data fields (item_gid, price, quantity)

**Tools:**
- Wireshark "Follow TCP Stream"
- Hex editor (HxD, Hex Fiend)
- Compare with Python LaBot message definitions

**Deliverable:**
- `PROTOCOL_ANALYSIS.md` document
- Message ID mapping table
- Example packet breakdown (annotated hex)

### T1.22: Document Message IDs and Format (1 day)
Create `protocol/MessageDefinitions.java`:
```java
public class MessageDefinitions {
    public static final int MSG_HDV_ITEMS = 0x????; // TBD
    public static final int MSG_HDV_PRICES = 0x????; // TBD

    public record HdvPriceMessage(
        int itemGid,
        int category,
        long price,
        int quantity
    ) {}
}
```

### T1.23: Create MessageDefinition Classes (1 day)
- HdvItemsMessage
- HdvPricesMessage
- Message base class

### T1.24: Implement BinaryReader Utility (1 day)
```java
public class BinaryReader {
    private final ByteBuffer buffer;

    public int readVarInt() {
        // Implement variable-length integer decoding
    }

    public String readUTF() {
        // Implement UTF string reading
    }

    public boolean readBoolean() {
        // Read bit-packed booleans
    }
}
```

### T1.25: Implement DofusRetroProtocolParser (2 days)
```java
@Service
public class DofusRetroProtocolParser {
    public ParsedMessage parse(byte[] rawPacket) {
        BinaryReader reader = new BinaryReader(rawPacket);

        int messageId = reader.readVarInt();
        int length = reader.readVarInt();

        return switch (messageId) {
            case MSG_HDV_PRICES -> parseHdvPrices(reader);
            default -> null;
        };
    }

    private HdvPriceMessage parseHdvPrices(BinaryReader reader) {
        // Parse HDV price packet
        int itemGid = reader.readVarInt();
        int category = reader.readVarInt();
        // ... extract price data
    }
}
```

### T1.26: Write Parser Tests (1 day)
- Use captured packets as test fixtures
- Store in `src/test/resources/packets/hdv-price.bin`
- Test parse accuracy (known packet → expected data)
- Test error handling (malformed packets)

**Validation:**
```bash
# Use real captured packet
byte[] packet = loadTestPacket("hdv-price.bin");
ParsedMessage msg = parser.parse(packet);
assertEquals(289, msg.itemGid); // Expected item ID
assertEquals(15, msg.price); // Expected price
```

**Deliverables:**
- PROTOCOL_ANALYSIS.md (protocol documentation)
- dofus-retro-hdv.pcap (captured packets)
- BinaryReader.java
- DofusRetroProtocolParser.java
- MessageDefinitions.java
- Test fixtures (packet samples)
- ParserTest.java (>90% coverage)

**Timeline:** 10 days (CRITICAL PATH)

**⚠️ RISK MITIGATION:**
If reverse engineering takes >2 weeks:
1. Implement partial parser (just item_gid + price)
2. Use mock parser for parallel development
3. Allocate additional time in week 4

**Blocks:** T2.1 (AGENT-BUSINESS needs parsed data)

Start now! This is the highest priority task.
```

---

#### 2.3.5 AGENT-DOCS (Track 1E)

```markdown
You are **AGENT-DOCS**, the Documentation Specialist.

**Mission:** Create initial project documentation (Tasks T1.27 - T1.29)

**Context:**
- Foundation (Phase 0) is complete
- Read: DOFUS_RETRO_PRD.md
- Read: IMPLEMENTATION_BOOK.md
- Branch: `feature/T1-documentation`

**Your Tasks:**

### T1.27: Update README with Java Setup (0.5 day)
Update `README.md`:
- Replace Python instructions with Java
- Add prerequisites (Java 26, Maven, Docker)
- Add quick start guide
- Add build instructions
- Add run instructions

### T1.28: Create Architecture Diagram (0.5 day)
Create `docs/architecture.md` with Mermaid diagrams:
- High-level component diagram
- Data flow diagram
- Module interaction diagram

### T1.29: Document Environment Setup (0.5 day)
Create `docs/setup.md`:
- Java 26 installation
- PostgreSQL setup
- Dofus Retro client setup
- Network permissions (pcap)
- IDE setup (IntelliJ/Eclipse)

**Deliverables:**
- Updated README.md
- docs/architecture.md
- docs/setup.md
- Mermaid diagrams

**Timeline:** 2 days

**Non-blocking:** Can work independently

Start now!
```

---

## 3. Phase-by-Phase Execution

### 3.1 Phase 0 Execution (Week 1)

**Single Agent Launch:**

```
Day 0:
→ Launch AGENT-INFRA with Phase 0 prompt
→ Monitor progress daily

Day 5:
→ Validate Phase 0 completion (run validation commands)
→ If successful: Proceed to Phase 1
→ If blocked: Debug with AGENT-INFRA
```

---

### 3.2 Phase 1 Execution (Week 2-3)

**Parallel Launch Strategy:**

```
Day 5 (immediately after Phase 0):
→ Launch 5 agents in parallel:
  1. AGENT-DATA (Track 1A)
  2. AGENT-NETWORK (Track 1B)
  3. AGENT-AUTOMATION (Track 1C)
  4. AGENT-PROTOCOL (Track 1D) - PRIORITY
  5. AGENT-DOCS (Track 1E)

Day 5-7:
→ Monitor AGENT-PROTOCOL (critical path)
→ Daily standups (check progress)

Day 10:
→ AGENT-DATA completes → Notify AGENT-BUSINESS
→ AGENT-NETWORK completes → Unblock AGENT-PROTOCOL

Day 15:
→ AGENT-PROTOCOL completes (critical milestone)
→ Phase 1 complete
→ Run Checkpoint 1 integration test
```

**Coordination:**
- Daily status check (all agents report progress)
- Blocker escalation (especially AGENT-PROTOCOL)
- Integration test before Phase 2

---

### 3.3 Phase 2 Execution (Week 4-5)

**Parallel Launch:**

```
Day 15 (after Phase 1 checkpoint):
→ Launch 3 agents in parallel:
  1. AGENT-BUSINESS (Track 2A) - waits for DATA + PROTOCOL
  2. AGENT-API (Track 2B) - waits for BUSINESS
  3. AGENT-INFRA (Track 2C) - config work

Day 15-17:
→ AGENT-BUSINESS integrates DATA + PROTOCOL
→ AGENT-INFRA externalizes config

Day 20:
→ AGENT-BUSINESS completes → Unblock AGENT-API
→ AGENT-API implements REST endpoints

Day 25:
→ Phase 2 complete
→ Run Checkpoint 2 integration test (full pipeline)
```

---

### 3.4 Phase 3 Execution (Week 6)

**Sequential with Parallel Testing:**

```
Day 25-27:
→ AGENT-INFRA: Wire all services (T3.1, T3.2)
→ AGENT-TEST: Prepare integration tests

Day 27-30:
→ Launch 3 agents in parallel:
  1. AGENT-TEST: E2E integration test (T3.3)
  2. AGENT-TEST: Performance test (T3.4)
  3. AGENT-TEST: Cross-platform test (T3.5)

→ AGENT-DOCS: OpenAPI docs, user guide (parallel)

Day 30:
→ Phase 3 complete
→ Run Checkpoint 3 (full application test)
```

---

### 3.5 Phase 4 Execution (Week 7)

**Deployment:**

```
Day 30-35:
→ AGENT-INFRA: Docker setup (T4.1, T4.2, T4.3)
→ AGENT-INFRA: CI/CD pipeline (T4.4)
→ AGENT-DOCS: Finalize docs (T4.5, T4.6)

Day 35:
→ AGENT-TEST: Final test suite (T4.7)
→ Release v1.0

Day 35+:
→ Deploy to production
→ Monitor and iterate
```

---

## 4. Parallel Launch Strategy

### 4.1 Maximum Parallelization Plan

**Phase 1 - Week 2:**
```
Agent Utilization: 5/9 (56%)

AGENT-DATA:      ████████████░░░░░░░░░░
AGENT-NETWORK:   ████████████████░░░░░░
AGENT-AUTOMATION:████████████████████░░
AGENT-PROTOCOL:  ░░░░████████████████░░ (waits for NETWORK)
AGENT-DOCS:      ████░░░░░░░░░░░░░░░░░░

Dependencies:
- PROTOCOL waits 2 days for NETWORK (T1.8)
- All others: fully parallel
```

**Phase 1 - Week 3:**
```
Agent Utilization: 4/9 (44%)

AGENT-AUTOMATION:████████████░░░░░░░░░░
AGENT-PROTOCOL:  ████████████████░░░░░░
AGENT-TEST:      ░░░░░░░░████████████░░ (starts testing completed modules)
AGENT-DOCS:      ░░░░░░░░░░░░████░░░░░░
```

**Phase 2 - Week 4-5:**
```
Agent Utilization: 4/9 (44%)

AGENT-BUSINESS:  ░░████████████░░░░░░░░
AGENT-API:       ░░░░░░░░░░░░████████░░
AGENT-INFRA:     ████████░░░░░░░░░░░░░░
AGENT-TEST:      ░░░░░░░░░░░░░░░░████░░
```

### 4.2 Handoff Protocol

**When Agent Completes Task:**

1. **Agent posts completion report:**
```markdown
**AGENT-{NAME} Completion Report**

**Task:** T{X.Y}
**Status:** ✅ COMPLETE
**Duration:** X days
**Branch:** feature/T{X.Y}-description

**Deliverables:**
- [x] {file 1}
- [x] {file 2}
- [x] Tests (coverage: X%)

**Acceptance Criteria:**
- [x] Criterion 1
- [x] Criterion 2

**Integration Points:**
This unblocks: T{Y.Z} (AGENT-{OTHER})

**Known Issues:**
- None / {list issues}

**Next Steps:**
Merge feature branch → Notify AGENT-{OTHER}
```

2. **Coordinator validates deliverables:**
```bash
git checkout feature/T1.1-jpa-entities
mvn clean test
# Review code
# Check acceptance criteria
```

3. **Merge and notify:**
```bash
git merge feature/T1.1-jpa-entities
git push origin main
# Notify dependent agents
```

4. **Launch dependent agent:**
```markdown
→ Launch AGENT-BUSINESS (T2.1 now unblocked)
```

---

## 5. Integration & Handoffs

### 5.1 Integration Checkpoint 1 (End of Phase 1)

**Goal:** Verify all modules work independently

**Test Script:**
```java
@Test
void checkpoint1_AllModulesWork() {
    // Test 1: Entities persist
    Item item = new Item();
    item.setItemGid(289);
    itemRepository.save(item);
    assertThat(itemRepository.findByItemGid(289)).isPresent();

    // Test 2: Packet capture receives data
    byte[] testPacket = loadTestPacket();
    packetCaptureService.start();
    // Send test packet
    assertThat(packetQueue).hasSize(1);

    // Test 3: Protocol parser extracts data
    ParsedMessage msg = protocolParser.parse(testPacket);
    assertThat(msg.getItemGid()).isEqualTo(289);

    // Test 4: Automation can click
    automationService.clickCategory(0);
    // Verify click occurred
}
```

**Pass Criteria:**
- All tests green
- No compilation errors
- Code coverage >60%

---

### 5.2 Integration Checkpoint 2 (End of Phase 2)

**Goal:** Verify end-to-end pipeline works

**Test Script:**
```java
@Test
@SpringBootTest
void checkpoint2_EndToEndPipeline() {
    // Simulate packet arrival
    byte[] hdvPacket = loadHdvPacket();
    packetQueue.offer(hdvPacket);

    // Wait for processing
    await().atMost(5, SECONDS).until(() ->
        itemRepository.count() > 0
    );

    // Verify data in database
    List<Item> items = itemRepository.findAll();
    assertThat(items).isNotEmpty();

    // Verify API returns data
    String json = restTemplate.getForObject(
        "http://localhost:8080/api/items",
        String.class
    );
    assertThat(json).contains("itemGid");
}
```

**Pass Criteria:**
- Full pipeline works
- API returns valid JSON
- Deduplication prevents duplicates

---

### 5.3 Integration Checkpoint 3 (End of Phase 3)

**Goal:** Verify full application with real game

**Manual Test:**
1. Start Dofus Retro client
2. Login and open HDV
3. Start Spring Boot application
4. Observe automation clicking categories
5. Wait 5 minutes
6. Query API: `GET /api/items`
7. Verify prices appear

**Pass Criteria:**
- Automation works without crashes
- Data collected and stored
- API serves real data
- No errors in logs

---

## 6. Troubleshooting

### 6.1 Common Issues

**Issue: Agent stuck/blocked**
```
Solution:
1. Check dependencies (are they complete?)
2. Review task scope (too large?)
3. Break into smaller subtasks
4. Provide mock data to unblock
```

**Issue: Integration test fails**
```
Solution:
1. Test modules independently first
2. Check interface contracts
3. Review data flow
4. Add debug logging
5. Use integration debugger
```

**Issue: Agent delivers incomplete work**
```
Solution:
1. Review acceptance criteria
2. Provide specific feedback
3. Re-assign with clearer scope
4. Add examples/references
```

**Issue: Critical path delayed (PROTOCOL)**
```
Solution:
1. Extend timeline (add week)
2. Simplify protocol (partial implementation)
3. Use mock parser temporarily
4. Allocate additional resources
```

---

### 6.2 Risk Mitigation

**If AGENT-PROTOCOL takes >2 weeks:**
- Implement stub parser with mock data
- Other agents continue with mocks
- Replace with real parser later

**If Pcap4j doesn't work:**
- Fallback to tcpdump file replay
- Use Python Scapy as subprocess
- Implement socket proxy instead

**If Sikuli too slow:**
- Use pure Java Robot (faster)
- Pre-compute template positions
- Reduce image matching frequency

---

## 7. Agent Status Dashboard

### Current Sprint: Phase 0

```
Active: AGENT-INFRA
Tasks:  T0.1 → T0.2 → T0.3 → T0.5 → T0.6 → T0.7
Status: 🟡 IN PROGRESS
ETA:    Day 5
```

### Next Sprint: Phase 1

```
Queued Agents:
- AGENT-DATA (ready to launch Day 5)
- AGENT-NETWORK (ready to launch Day 5)
- AGENT-AUTOMATION (ready to launch Day 5)
- AGENT-PROTOCOL (ready to launch Day 5)
- AGENT-DOCS (ready to launch Day 5)

Launch Plan:
Day 5: Launch all 5 agents in parallel
Day 10: First checkpoint (AGENT-DATA complete)
Day 15: Phase 1 complete (all agents done)
```

---

## 8. Quick Reference

### 8.1 Agent Launch Commands

```bash
# Phase 0
./launch-agent.sh INFRA "Phase 0: Foundation"

# Phase 1 (parallel)
./launch-agent.sh DATA "Phase 1 Track 1A: Data Layer" &
./launch-agent.sh NETWORK "Phase 1 Track 1B: Network Capture" &
./launch-agent.sh AUTOMATION "Phase 1 Track 1C: GUI Automation" &
./launch-agent.sh PROTOCOL "Phase 1 Track 1D: Protocol Parser" &
./launch-agent.sh DOCS "Phase 1 Track 1E: Documentation" &
wait

# Phase 2 (parallel)
./launch-agent.sh BUSINESS "Phase 2 Track 2A: Business Logic" &
./launch-agent.sh API "Phase 2 Track 2B: REST API" &
./launch-agent.sh INFRA "Phase 2 Track 2C: Configuration" &
wait

# Phase 3
./launch-agent.sh TEST "Phase 3: Integration Testing" &
./launch-agent.sh DOCS "Phase 3: API Documentation" &
wait

# Phase 4
./launch-agent.sh INFRA "Phase 4: Deployment"
```

### 8.2 Validation Commands

```bash
# Phase 0 validation
mvn clean install && mvn spring-boot:run
curl http://localhost:8080/actuator/health

# Phase 1 validation
mvn test -Dtest=*RepositoryTest
mvn test -Dtest=PacketCaptureServiceTest
mvn test -Dtest=ProtocolParserTest

# Phase 2 validation
mvn verify
curl http://localhost:8080/api/items
curl http://localhost:8080/api/items/1/prices

# Phase 3 validation
mvn integration-test
./run-e2e-test.sh

# Phase 4 validation
docker-compose up -d
curl http://localhost:8080/api/health
```

---

## END OF AGENT DELEGATION GUIDE

**Usage:**
1. Follow phase-by-phase execution plan
2. Use prompt templates to launch agents
3. Monitor progress with checkpoints
4. Coordinate handoffs at integration points

**Next:** Launch AGENT-INFRA with Phase 0 template!

---

# Refined Agent Roster & Wave-Based Execution Plan
# Dofus Retro Price Tracker - Multi-Agent Coordination

**Version:** 2.0
**Date:** 2025-11-08
**Updates:** Added AGENT-FRONT, AGENT-REVIEW, AGENT-SECURITY

---

## 📋 Table of Contents

1. [Agent Roster Analysis](#1-agent-roster-analysis)
2. [Final Agent Profiles](#2-final-agent-profiles)
3. [Wave-Based Execution Model](#3-wave-based-execution-model)
4. [Review Agent Protocol](#4-review-agent-protocol)
5. [Updated Task Allocation](#5-updated-task-allocation)
6. [Agent Interaction Matrix](#6-agent-interaction-matrix)

---

## 1. Agent Roster Analysis

### 1.1 Your Suggested Agents - Evaluation

| Agent | Keep? | Rationale |
|-------|-------|-----------|
| 🏗️ AGENT-INFRA | ✅ **KEEP** | Critical for project setup, Docker, CI/CD |
| 🗄️ AGENT-DATA | ✅ **KEEP** | Specialized in JPA/Hibernate, database design |
| 📡 AGENT-NETWORK | ✅ **KEEP** | Specialized in Pcap4j, packet capture |
| 🔐 AGENT-PROTOCOL | ✅ **KEEP** | Critical for reverse engineering Dofus Retro protocol |
| 🤖 AGENT-AUTOMATION | ✅ **KEEP** | GUI automation is complex enough to warrant dedicated agent |
| 💼 AGENT-BUSINESS | ✅ **KEEP** | Service layer, caching, business logic |
| 🌐 AGENT-API | ✅ **KEEP** | REST controllers, API design |
| 🧪 AGENT-TEST | ✅ **KEEP** | Unit + integration testing |
| 📚 AGENT-DOCS | ✅ **KEEP** | Documentation throughout project |
| 🖥️ AGENT-FRONT | ✅ **ADD** | Frontend visualization (Angular 20) - significant scope |
| ☕ AGENT-BACK | ❌ **REMOVE** | Redundant: DATA + BUSINESS + API cover all backend |
| 👓 AGENT-REVIEW | ✅ **ADD** | **CRITICAL**: Quality gates, code review, acceptance validation |

### 1.2 Additional Recommended Agents

| Agent | Add? | Rationale |
|-------|------|-----------|
| 🔒 AGENT-SECURITY | ✅ **ADD** | Security audit, OWASP checks, vulnerability scanning |
| ⚡ AGENT-INTEGRATION | ⚠️ **MERGE** | Merge into AGENT-TEST (integration testing subtasks) |
| 🎯 AGENT-PERFORMANCE | ⚠️ **MERGE** | Merge into AGENT-TEST (performance testing subtasks) |

### 1.3 Final Decision

**Total Agents: 12**

**Core Development (9 agents):**
1. AGENT-INFRA
2. AGENT-DATA
3. AGENT-NETWORK
4. AGENT-PROTOCOL
5. AGENT-AUTOMATION
6. AGENT-BUSINESS
7. AGENT-API
8. AGENT-FRONT (NEW)
9. AGENT-DOCS

**Quality Assurance (3 agents):**
10. AGENT-TEST
11. AGENT-REVIEW (NEW - runs after each wave)
12. AGENT-SECURITY (NEW - runs before deployment)

---

## 2. Final Agent Profiles

### 2.1 Development Agents (Existing)

*[Previous 9 agents remain unchanged - see IMPLEMENTATION_BOOK.md Section 1.1]*

---

### 2.2 NEW: AGENT-FRONT - Frontend Development Specialist

#### Profile

**Expertise:** Angular 20, TypeScript, data visualization, REST API integration
**Responsibilities:**
- Angular component development
- Time-series chart implementation
- API client integration
- Responsive UI design
- State management
**Key Skills:** Angular 20, TypeScript, Angular Material, Chart.js/D3.js, HttpClient, Angular Router
**Works Best With:** AGENT-API (consumes REST endpoints)

#### Scope

**Primary Tasks:**
- Design and implement Angular 20 frontend
- Create price visualization charts (time-series)
- Implement item search/filter interface
- Connect to backend REST API
- Responsive design (desktop + tablet)

**Technology Stack:**
- React 18 (with hooks)
- TypeScript
- Recharts or D3.js (for price charts)
- Axios (HTTP client)
- TailwindCSS or Material-UI
- Vite or Create React App

#### Deliverables

- React application (src/frontend/)
- Components: ItemSelector, PriceChart, Dashboard
- API client service (apiClient.ts)
- Unit tests (React Testing Library)
- Storybook components (optional)

---

### 2.3 NEW: AGENT-REVIEW - Code Review & Quality Gate Specialist

#### Profile

**Expertise:** Code review, architecture validation, acceptance testing, quality metrics
**Responsibilities:**
- Review deliverables after each wave
- Validate acceptance criteria
- Ensure code quality standards
- Verify integration points
- Gate-keep phase transitions
**Key Skills:** Code review, static analysis, architecture patterns, testing best practices
**Works Best With:** ALL agents (reviews their work)

#### Review Scope

**Wave Reviews (After Each Phase):**
- Code quality (SonarQube metrics, code smells)
- Test coverage (>80% unit, >60% integration)
- Architecture compliance (follows PRD)
- Acceptance criteria validation
- Integration point verification
- Documentation completeness

**Gate-Keeping:**
- BLOCKS next phase if critical issues found
- Provides feedback to agents
- Requests rework if necessary
- Approves phase completion

#### Review Checklist Template

```markdown
## AGENT-REVIEW: Wave X Completion Review

### Code Quality
- [ ] SonarQube rating: A (no critical issues)
- [ ] No code smells (or documented exceptions)
- [ ] Consistent coding style (Google Java Style)
- [ ] Proper error handling

### Testing
- [ ] Unit test coverage: >80%
- [ ] Integration test coverage: >60%
- [ ] All tests passing
- [ ] Test quality (meaningful assertions)

### Architecture
- [ ] Follows PRD design
- [ ] Proper separation of concerns
- [ ] SOLID principles applied
- [ ] Spring best practices

### Acceptance Criteria
- [ ] All task criteria met
- [ ] Deliverables complete
- [ ] Documentation updated
- [ ] Integration points validated

### Integration
- [ ] Module integrates with dependencies
- [ ] APIs/interfaces stable
- [ ] No breaking changes
- [ ] Integration tests pass

### Security
- [ ] No hardcoded secrets
- [ ] Input validation present
- [ ] SQL injection prevention
- [ ] Proper authentication/authorization (if applicable)

### Performance
- [ ] No obvious performance issues
- [ ] Efficient queries (no N+1)
- [ ] Proper indexing (database)
- [ ] Resource cleanup (connections, threads)

### Documentation
- [ ] JavaDoc for public APIs
- [ ] README updated
- [ ] Architecture diagrams current
- [ ] API documentation (OpenAPI)

### Decision: ✅ APPROVE / ⚠️ APPROVE WITH NOTES / ❌ REJECT

**Feedback for Agents:**
- {Specific feedback}

**Required Rework:**
- {List of items requiring fixes}

**Approved for Phase X+1:** {Yes/No}
```

---

### 2.4 NEW: AGENT-SECURITY - Security & Vulnerability Specialist

#### Profile

**Expertise:** Security best practices, OWASP Top 10, vulnerability scanning, secure coding
**Responsibilities:**
- Security audit before deployment
- Vulnerability scanning (dependencies)
- Code security review
- Penetration testing (basic)
- Security documentation
**Key Skills:** OWASP, dependency-check, Snyk, secure coding, threat modeling
**Works Best With:** AGENT-REVIEW (part of quality gates)

#### Security Scope

**Phase 4 Security Audit:**
- OWASP Top 10 vulnerability check
- Dependency vulnerability scan (Maven)
- SQL injection prevention validation
- XSS prevention (if web UI)
- Authentication/authorization review
- Secrets management audit
- Network security (packet capture privileges)

**Tools:**
- OWASP Dependency-Check
- Snyk or Dependabot
- SonarQube security rules
- Manual code review

#### Security Checklist

```markdown
## AGENT-SECURITY: Security Audit Report

### Dependency Vulnerabilities
- [ ] Maven dependency-check run
- [ ] No high/critical vulnerabilities
- [ ] All dependencies up-to-date

### OWASP Top 10
- [ ] A01: Broken Access Control - N/A (no auth yet)
- [ ] A02: Cryptographic Failures - ✓ No sensitive data exposed
- [ ] A03: Injection - ✓ Parameterized queries
- [ ] A04: Insecure Design - ✓ Architecture reviewed
- [ ] A05: Security Misconfiguration - ✓ Configs reviewed
- [ ] A06: Vulnerable Components - ✓ Dependencies checked
- [ ] A07: Identification Failures - N/A
- [ ] A08: Software Integrity Failures - ✓ CI/CD secure
- [ ] A09: Logging Failures - ✓ Proper logging
- [ ] A10: SSRF - N/A

### Code Security
- [ ] No hardcoded credentials
- [ ] Environment variables for secrets
- [ ] Input validation (API endpoints)
- [ ] Output encoding (if applicable)
- [ ] Proper error messages (no info leak)

### Network Security
- [ ] Pcap4j privilege requirements documented
- [ ] Packet capture read-only (no injection)
- [ ] Network filter properly configured

### Deployment Security
- [ ] Docker container non-root user
- [ ] Minimal container image (no unnecessary tools)
- [ ] Secrets not in Docker image
- [ ] HTTPS recommended (if exposed)

### Decision: ✅ APPROVE / ⚠️ APPROVE WITH NOTES / ❌ FAIL

**Security Issues Found:**
- {List of issues with severity}

**Recommendations:**
- {Security improvements}

**Approved for Production:** {Yes/No}
```

---

## 3. Wave-Based Execution Model

### 3.1 Wave Structure (Revised)

Each wave ends with **AGENT-REVIEW** validation before proceeding.

```
WAVE 0: Foundation (Week 1)
  → AGENT-INFRA executes
  → AGENT-REVIEW validates
  ✅ Gate: Foundation approved

WAVE 1: Core Modules (Week 2-3)
  → 5 agents execute in parallel
  → AGENT-REVIEW validates (Checkpoint 1)
  ✅ Gate: Core modules approved

WAVE 2: Integration (Week 4-5)
  → 3 agents execute in parallel
  → AGENT-REVIEW validates (Checkpoint 2)
  ✅ Gate: Integration approved

WAVE 3: Frontend + Testing (Week 6)
  → AGENT-FRONT + AGENT-TEST execute
  → AGENT-REVIEW validates (Checkpoint 3)
  ✅ Gate: Testing approved

WAVE 4: Security + Deployment (Week 7)
  → AGENT-INFRA + AGENT-SECURITY execute
  → AGENT-REVIEW validates (Final Gate)
  → AGENT-SECURITY validates (Security Gate)
  ✅ Gate: Production ready
```

### 3.2 Review Gates

**Gate 0: Foundation Review**
- Time: End of Week 1
- Reviewer: AGENT-REVIEW
- Criteria: Project builds, DB runs, structure correct
- Blocker: If fail, fix before Wave 1

**Gate 1: Core Modules Review**
- Time: End of Week 3
- Reviewer: AGENT-REVIEW
- Criteria: All modules work independently, tests pass, coverage >60%
- Blocker: If fail, fix before Wave 2

**Gate 2: Integration Review**
- Time: End of Week 5
- Reviewer: AGENT-REVIEW
- Criteria: Full pipeline works, API functional, coverage >70%
- Blocker: If fail, fix before Wave 3

**Gate 3: Testing Review**
- Time: End of Week 6
- Reviewer: AGENT-REVIEW
- Criteria: E2E tests pass, frontend works, coverage >80%
- Blocker: If fail, fix before Wave 4

**Gate 4: Security + Deployment Review**
- Time: End of Week 7
- Reviewer: AGENT-REVIEW + AGENT-SECURITY
- Criteria: Security audit pass, Docker works, docs complete
- Blocker: If fail, cannot deploy

---

## 4. Review Agent Protocol

### 4.1 Review Workflow

**Step 1: Wave Completion Signal**
```
All agents in wave complete → Notify AGENT-REVIEW
```

**Step 2: AGENT-REVIEW Execution**
```markdown
You are AGENT-REVIEW, the Code Review & Quality Gate Specialist.

**Mission:** Review Wave X deliverables and approve/reject for next phase.

**Context:**
- Wave X has completed
- All agents have submitted deliverables
- Review checklist: [see template above]

**Your Tasks:**
1. Clone all feature branches
2. Run static analysis (SonarQube, checkstyle)
3. Review test coverage (JaCoCo report)
4. Manually review critical code sections
5. Verify acceptance criteria for each task
6. Run integration tests
7. Check documentation completeness
8. Complete review checklist
9. Provide feedback to agents
10. Approve/reject wave completion

**Deliverables:**
- Completed review checklist
- Feedback document for each agent
- Approval/rejection decision
- List of required fixes (if rejected)

**Timeline:** 1 day

Start now!
```

**Step 3: Feedback Loop**
```
AGENT-REVIEW provides feedback → Agents fix issues → Re-review
```

**Step 4: Gate Approval**
```
✅ APPROVED → Proceed to next wave
❌ REJECTED → Fix and re-submit
⚠️ APPROVED WITH NOTES → Proceed but track tech debt
```

### 4.2 Review Cadence

| Wave | Review Effort | Review Duration |
|------|---------------|-----------------|
| Wave 0 | Low | 0.5 day |
| Wave 1 | High | 1.5 days (5 agents to review) |
| Wave 2 | Medium | 1 day |
| Wave 3 | Medium | 1 day |
| Wave 4 | High | 1 day (+ AGENT-SECURITY) |

---

## 5. Updated Task Allocation

### 5.1 Frontend Tasks (NEW)

#### WAVE 3: Frontend Development (AGENT-FRONT)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **T3.8** | FRONT | Setup Angular 20 + TypeScript project (Angular CLI) | 0.5 day | BLOCKS T3.9 | 🔴 TODO |
| **T3.9** | FRONT | Create API client service (Axios) | 0.5 day | BLOCKS T3.10 | 🔴 TODO |
| **T3.10** | FRONT | Implement ItemSelector component | 1 day | BLOCKS T3.11 | 🔴 TODO |
| **T3.11** | FRONT | Implement PriceChart component (Recharts) | 2 days | BLOCKS T3.12 | 🔴 TODO |
| **T3.12** | FRONT | Implement Dashboard layout | 1 day | BLOCKS T3.13 | 🔴 TODO |
| **T3.13** | FRONT | Add filters (date range, quantity) | 1 day | Non-blocking | 🔴 TODO |
| **T3.14** | FRONT | Style with TailwindCSS | 1 day | Non-blocking | 🔴 TODO |
| **T3.15** | FRONT | Write component tests (RTL) | 1 day | Non-blocking | 🔴 TODO |
| **T3.16** | FRONT | Create production build | 0.5 day | BLOCKS deployment | 🔴 TODO |

**Dependencies:** T2.7-T2.10 (AGENT-API REST endpoints)
**Output:** Angular 20 frontend application
**Timeline:** 8 days

---

### 5.2 Review Tasks (NEW)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **TR0.1** | REVIEW | Review Wave 0 (Foundation) | 0.5 day | **GATES Wave 1** | 🔴 TODO |
| **TR1.1** | REVIEW | Review Wave 1 (Core Modules) | 1.5 days | **GATES Wave 2** | 🔴 TODO |
| **TR2.1** | REVIEW | Review Wave 2 (Integration) | 1 day | **GATES Wave 3** | 🔴 TODO |
| **TR3.1** | REVIEW | Review Wave 3 (Frontend + Testing) | 1 day | **GATES Wave 4** | 🔴 TODO |
| **TR4.1** | REVIEW | Final review before deployment | 1 day | **GATES Release** | 🔴 TODO |

---

### 5.3 Security Tasks (NEW)

| Task ID | Agent | Description | Duration | Blocking | Status |
|---------|-------|-------------|----------|----------|--------|
| **TS4.1** | SECURITY | Run dependency vulnerability scan | 0.5 day | BLOCKS deployment | 🔴 TODO |
| **TS4.2** | SECURITY | OWASP Top 10 compliance check | 1 day | BLOCKS deployment | 🔴 TODO |
| **TS4.3** | SECURITY | Code security review | 1 day | BLOCKS deployment | 🔴 TODO |
| **TS4.4** | SECURITY | Docker security audit | 0.5 day | BLOCKS deployment | 🔴 TODO |
| **TS4.5** | SECURITY | Create security documentation | 0.5 day | Non-blocking | 🔴 TODO |

**Dependencies:** All Wave 3 tasks complete
**Output:** Security audit report, fixes applied
**Timeline:** 3 days

---

## 6. Agent Interaction Matrix

### 6.1 Dependency Graph (Updated)

```
WAVE 0:
AGENT-INFRA → AGENT-REVIEW (Gate 0)
                    ↓
              [Wave 1 unlocked]

WAVE 1:
AGENT-DATA ──────┐
AGENT-NETWORK ───┤
AGENT-AUTOMATION ┼→ AGENT-REVIEW (Gate 1)
AGENT-PROTOCOL ──┤         ↓
AGENT-DOCS ──────┘   [Wave 2 unlocked]

WAVE 2:
AGENT-BUSINESS ──┐
AGENT-API ────── ┼→ AGENT-REVIEW (Gate 2)
AGENT-INFRA ────┘         ↓
              [Wave 3 unlocked]

WAVE 3:
AGENT-FRONT ────┐
AGENT-TEST ─────┼→ AGENT-REVIEW (Gate 3)
AGENT-DOCS ────┘         ↓
              [Wave 4 unlocked]

WAVE 4:
AGENT-INFRA ────┐
AGENT-SECURITY ─┼→ AGENT-REVIEW (Gate 4)
AGENT-DOCS ─────┘         ↓
                    [RELEASE]
```

### 6.2 Agent Collaboration Patterns

**Parallel Execution (No Dependencies):**
- Wave 1: DATA + AUTOMATION + DOCS (can work simultaneously)
- Wave 1: NETWORK → (2 days wait) → PROTOCOL (sequential)

**Sequential Execution (Dependencies):**
- PROTOCOL must wait for NETWORK (packet structure)
- BUSINESS must wait for DATA + PROTOCOL (entities + parser)
- API must wait for BUSINESS (service layer)
- FRONT must wait for API (REST endpoints)

**Review Points (Synchronization):**
- After each wave, ALL agents pause
- AGENT-REVIEW validates deliverables
- Agents address feedback
- Gate opens for next wave

---

## 7. Updated Timeline

### 7.1 Timeline with Review Gates

```
Week 1: Foundation
  Days 1-5: AGENT-INFRA
  Day 5.5:  AGENT-REVIEW (Gate 0)
  ✅ Gate: Foundation approved

Week 2-3: Core Modules
  Days 6-15: AGENT-DATA, NETWORK, AUTOMATION, PROTOCOL, DOCS (parallel)
  Days 16-17: AGENT-REVIEW (Gate 1)
  ✅ Gate: Core modules approved

Week 4-5: Integration
  Days 18-27: AGENT-BUSINESS, API, INFRA (parallel)
  Day 28: AGENT-REVIEW (Gate 2)
  ✅ Gate: Integration approved

Week 6: Frontend + Testing
  Days 29-35: AGENT-FRONT, TEST, DOCS (parallel)
  Day 36: AGENT-REVIEW (Gate 3)
  ✅ Gate: Testing approved

Week 7: Security + Deployment
  Days 37-40: AGENT-INFRA, SECURITY (parallel)
  Day 41: AGENT-REVIEW (Gate 4)
  Day 42: AGENT-SECURITY (Security Gate)
  ✅ Gate: Production ready

TOTAL: 8 weeks (with review gates)
```

### 7.2 Critical Path (Updated)

```
INFRA (1w) → REVIEW (0.5d) →
  NETWORK (5d) → PROTOCOL (10d) → REVIEW (1.5d) →
    BUSINESS (5d) → API (3d) → REVIEW (1d) →
      FRONT (8d) → REVIEW (1d) →
        SECURITY (3d) → REVIEW (1d)

Total: 43 days (8.6 weeks)
```

---

## 8. Execution Commands

### 8.1 Wave 0 Launch

```bash
# Launch AGENT-INFRA
./launch-agent.sh INFRA "Wave 0: Foundation"

# After completion (Day 5)
./launch-agent.sh REVIEW "Review Wave 0" --checklist TR0.1
```

### 8.2 Wave 1 Launch (Parallel)

```bash
# Launch all agents simultaneously
./launch-agent.sh DATA "Wave 1 Track 1A" &
./launch-agent.sh NETWORK "Wave 1 Track 1B" &
./launch-agent.sh AUTOMATION "Wave 1 Track 1C" &
./launch-agent.sh PROTOCOL "Wave 1 Track 1D" &
./launch-agent.sh DOCS "Wave 1 Track 1E" &
wait

# After completion (Day 15)
./launch-agent.sh REVIEW "Review Wave 1" --checklist TR1.1
```

### 8.3 Wave 2 Launch

```bash
./launch-agent.sh BUSINESS "Wave 2 Track 2A" &
./launch-agent.sh API "Wave 2 Track 2B" &
./launch-agent.sh INFRA "Wave 2 Track 2C" &
wait

./launch-agent.sh REVIEW "Review Wave 2" --checklist TR2.1
```

### 8.4 Wave 3 Launch (Frontend + Testing)

```bash
./launch-agent.sh FRONT "Wave 3: Frontend" &
./launch-agent.sh TEST "Wave 3: Integration Tests" &
./launch-agent.sh DOCS "Wave 3: Documentation" &
wait

./launch-agent.sh REVIEW "Review Wave 3" --checklist TR3.1
```

### 8.5 Wave 4 Launch (Security + Deployment)

```bash
./launch-agent.sh INFRA "Wave 4: Deployment" &
./launch-agent.sh SECURITY "Wave 4: Security Audit" &
wait

./launch-agent.sh REVIEW "Review Wave 4" --checklist TR4.1
./launch-agent.sh SECURITY "Security Gate" --final-audit
```

---

## 9. Summary

### 9.1 Final Agent Roster

**12 Agents Total:**

**Development (9):**
1. AGENT-INFRA
2. AGENT-DATA
3. AGENT-NETWORK
4. AGENT-PROTOCOL
5. AGENT-AUTOMATION
6. AGENT-BUSINESS
7. AGENT-API
8. AGENT-FRONT ✨ NEW
9. AGENT-DOCS

**Quality Assurance (3):**
10. AGENT-TEST
11. AGENT-REVIEW ✨ NEW (critical!)
12. AGENT-SECURITY ✨ NEW

### 9.2 Key Changes from v1.0

✅ **Added AGENT-FRONT** - Handles Angular 20 visualization (8 tasks)
✅ **Added AGENT-REVIEW** - Quality gates after each wave (5 review tasks)
✅ **Added AGENT-SECURITY** - Security audit before deployment (5 tasks)
❌ **Removed AGENT-BACK** - Redundant with existing backend agents

### 9.3 New Task Count

**Total Tasks: 93** (was 73)
- Frontend: +9 tasks
- Review: +5 tasks
- Security: +5 tasks
- Updated timeline: 8 weeks (was 7 weeks)

### 9.4 Quality Benefits

**With AGENT-REVIEW:**
- Catches issues early (after each wave)
- Ensures acceptance criteria met
- Maintains code quality throughout
- Prevents technical debt accumulation
- Gates prevent broken work from propagating

**With AGENT-SECURITY:**
- Production-ready security posture
- No critical vulnerabilities
- OWASP compliance
- Secure deployment

---

## 10. Next Steps

### 10.1 Immediate Actions

1. ✅ Review this agent roster (approved?)
2. 📝 Update IMPLEMENTATION_BOOK.md with frontend tasks
3. 📝 Update AGENT_DELEGATION_GUIDE.md with new agent prompts
4. 🚀 Launch AGENT-INFRA for Wave 0

### 10.2 Questions for You

1. **Approve this agent roster?** (12 agents)
2. **Frontend technology preference?** (Angular 20 + Angular Material confirmed)
3. **Review gate strictness?** (Block on any issue vs. warning-only?)
4. **Security depth?** (Basic audit or comprehensive pentest?)
5. **Ready to launch Wave 0?**

---

**END OF REFINED AGENT ROSTER**

Ready to proceed with Wave 0 launch? 🚀

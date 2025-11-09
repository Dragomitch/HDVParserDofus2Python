# Contributing Guidelines

Thank you for your interest in contributing to the Dofus Retro Price Tracker project! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Style Guide](#code-style-guide)
- [Testing Requirements](#testing-requirements)
- [Git Workflow](#git-workflow)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)
- [Code of Conduct](#code-of-conduct)

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Setup Development Environment

Follow the [Development Environment Setup Guide](docs/setup.md) to get your environment ready.

## Development Workflow

### 1. Choose an Issue or Feature

- Check [GitHub Issues](https://github.com/yourrepo/issues) for open issues
- Look for issues labeled `good first issue` or `help wanted`
- Comment on the issue to express interest
- Wait for maintainer approval before starting work

### 2. Create a Feature Branch

```bash
# Update main branch
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/description

# Branch naming conventions:
# feature/add-item-search      - New feature
# fix/database-connection      - Bug fix
# docs/update-readme           - Documentation
# refactor/simplify-parser     - Refactoring
# perf/optimize-queries        - Performance
# test/add-item-tests          - Tests
```

### 3. Develop Your Feature

```bash
# Make changes to your code
# Keep commits small and focused
# Write tests for your changes
# Run tests frequently

# Before pushing:
mvn clean install       # Full build and test
```

### 4. Commit Your Changes

```bash
# Stage changes
git add .

# Commit with clear message
git commit -m "feat: add item search functionality"

# Commit message format: <type>: <description>
# Types: feat, fix, docs, style, refactor, perf, test, chore
# Keep message under 50 characters
```

### 5. Push and Create Pull Request

```bash
# Push to remote
git push origin feature/description

# Create pull request on GitHub
# Fill in PR template with description and details
```

## Code Style Guide

### Java Code Style

Follow **Google Java Style Guide** with project-specific modifications:

#### Naming Conventions

**Classes & Interfaces:**
```java
// PascalCase
public class ItemService { }
public interface ItemRepository { }
public class ItemNotFoundExeption extends Exception { }
```

**Methods & Variables:**
```java
// camelCase
public void recordPrice() { }
private String itemName;
```

**Constants:**
```java
// UPPER_SNAKE_CASE
private static final int MAX_ITEMS = 1000;
private static final String DEFAULT_CHARSET = "UTF-8";
```

**Packages:**
```java
// lowercase, dot-separated
com.dofusretro.pricetracker.service
com.dofusretro.pricetracker.model
```

#### Code Organization

```java
public class Item {
    // 1. Static fields
    private static final Logger logger = LoggerFactory.getLogger(Item.class);

    // 2. Instance fields
    private Long id;
    private String name;

    // 3. Constructors
    public Item() { }
    public Item(String name) { }

    // 4. Public methods
    public void recordPrice() { }

    // 5. Protected methods
    protected void validatePrice() { }

    // 6. Private methods
    private void logPrice() { }

    // 7. Getters/Setters (use Lombok)
    // @Getter @Setter applied at class level
}
```

#### Formatting

```java
// Line length: 120 characters max
// Indentation: 4 spaces (no tabs)

// Braces on same line
if (condition) {
    doSomething();
}

// Method parameters on multiple lines if needed
public void savePrice(Item item,
                      Long price,
                      Integer quantity) {
    // implementation
}

// Don't use star imports
import com.dofusretro.pricetracker.model.Item;    // OK
// import com.dofusretro.pricetracker.model.*;    // NOT OK
```

### Using Lombok

Use Lombok to reduce boilerplate code:

```java
@Entity
@Table(name = "items")
@Data                          // @Getter @Setter @ToString @EqualsAndHashCode
@NoArgsConstructor            // No-arg constructor
@AllArgsConstructor           // All-args constructor
@Builder                      // Builder pattern
public class Item {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private SubCategory subCategory;
}

// Usage:
Item item = Item.builder()
    .id(1L)
    .name("Iron Ore")
    .build();
```

### JavaDoc Standards

Document public APIs with JavaDoc:

```java
/**
 * Records a price entry for an item in the auction house.
 *
 * <p>This method validates the price data and stores it in the database.
 * Duplicate entries within the same minute are ignored.
 *
 * @param item the item to record price for (not null)
 * @param price the price in Kamas (must be positive)
 * @param quantity the quantity available (must be positive)
 * @throws ItemNotFoundException if item does not exist
 * @throws InvalidPriceException if price is invalid
 * @return the created PriceEntry
 */
public PriceEntry recordPrice(Item item, Long price, Integer quantity) {
    // implementation
}
```

### Comments

```java
// Bad: Obvious comments
int price = 100;  // Set price to 100

// Good: Explain why, not what
// Using Caffeine cache with 5-minute TTL for frequently accessed items
@Cacheable(value = "items", cacheManager = "itemCacheManager")
public Item getItemById(Long id) {
    // implementation
}

// Complex logic needs explanation
// Algorithm: Binary search for price history boundary
// Time complexity: O(log n)
private int findPriceHistoryBoundary(List<PriceEntry> prices, LocalDateTime date) {
    // implementation
}
```

## Testing Requirements

### Test Coverage Goals

- **Minimum coverage:** 80% (preferably >85%)
- **Service layer:** 100% of public methods
- **Controller layer:** Happy path + error scenarios
- **Repository layer:** Critical queries
- **Model layer:** Entity validation

### Unit Test Structure

```java
@SpringBootTest
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        // Common setup
    }

    // Test naming: test{Method}{Scenario}{Expected}

    @Test
    void testGetItemById_WithValidId_ReturnsItem() {
        // Arrange
        Long itemId = 1L;
        Item expectedItem = new Item(itemId, "Iron Ore");
        when(itemRepository.findById(itemId))
            .thenReturn(Optional.of(expectedItem));

        // Act
        Item actualItem = itemService.getItemById(itemId);

        // Assert
        assertThat(actualItem)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", "Iron Ore");

        verify(itemRepository).findById(itemId);
    }

    @Test
    void testGetItemById_WithInvalidId_ThrowsException() {
        // Arrange
        Long invalidId = -1L;
        when(itemRepository.findById(invalidId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItemById(invalidId))
            .isInstanceOf(ItemNotFoundException.class)
            .hasMessage("Item not found");
    }

    @Test
    void testRecordPrice_WithValidData_SavesPrice() {
        // Arrange
        Item item = new Item(1L, "Iron Ore");
        PriceEntry priceEntry = new PriceEntry();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(priceEntry);

        // Act
        PriceEntry result = itemService.recordPrice(item, 100L, 50);

        // Assert
        assertThat(result).isNotNull();
        verify(itemRepository).save(any(PriceEntry.class));
    }
}
```

### Using AssertJ

Prefer AssertJ for fluent assertions:

```java
// Good: AssertJ
assertThat(prices)
    .isNotEmpty()
    .hasSizeGreaterThan(5)
    .allMatch(p -> p.getPrice() > 0)
    .extracting(PriceEntry::getPrice)
    .containsExactly(100L, 200L, 300L);

// Avoid: Traditional JUnit assertions
// assertTrue(prices.size() > 5);  // Less readable
// assertEquals(100L, prices.get(0).getPrice());
```

### Test Execution

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ItemServiceTest

# Run with code coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS
start target/site/jacoco/index.html # Windows
xdg-open target/site/jacoco/index.html # Linux

# Run integration tests
mvn verify
```

## Git Workflow

### Commit Messages

Use conventional commit format:

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Code style (formatting, semicolons, etc.)
- `refactor` - Code refactoring
- `perf` - Performance improvement
- `test` - Adding tests
- `chore` - Build tools, dependencies, etc.

**Examples:**

```bash
git commit -m "feat: add item search functionality"

git commit -m "fix: correct packet parsing for price messages

Fixes issue where negative prices were accepted due to incorrect
integer overflow check. Now validates price range before storing."

git commit -m "docs: update setup guide with Docker instructions"

git commit -m "refactor: simplify protocol parser logic

- Extracted method for packet validation
- Improved error handling for malformed packets
- Reduced cyclomatic complexity"
```

### Keeping Branch Updated

```bash
# Fetch latest changes
git fetch origin

# Rebase on main (preferred for feature branches)
git rebase origin/main

# Or merge if rebase causes issues
git merge origin/main
```

### Squashing Commits

If you have multiple small commits, squash them before PR:

```bash
# Rebase last N commits
git rebase -i HEAD~3

# Mark commits to squash as 's' instead of 'p'
# Save file, and commits will be combined
```

## Pull Request Process

### Before Submitting

```bash
# Ensure all changes are committed
git status

# Ensure branch is up to date
git fetch origin
git rebase origin/main

# Run full test suite
mvn clean install

# Build passes with no warnings
mvn clean compile -Dorg.slf4j.simpleLogger.defaultLogLevel=warn

# Code coverage is above 80%
mvn jacoco:report
```

### PR Checklist

When creating a PR, include this checklist in the description:

```markdown
- [ ] Tests pass locally: `mvn test`
- [ ] Code coverage is above 80%: `mvn jacoco:report`
- [ ] Code follows style guide (Google Java Style)
- [ ] Documentation updated (README, JavaDoc, etc.)
- [ ] No merge conflicts with main branch
- [ ] Commit messages follow conventional commits format
- [ ] Related issue is referenced (e.g., "Fixes #123")
```

### PR Description Template

```markdown
## Description
Brief description of the changes and why they were made.

## Related Issue
Fixes #(issue number)

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Documentation update
- [ ] Refactoring
- [ ] Performance improvement

## Testing
How was this tested? (describe test cases added)

## Screenshots (if applicable)
If this adds UI changes, include screenshots.

## Checklist
- [ ] Tests pass locally
- [ ] Code coverage >80%
- [ ] Documentation updated
- [ ] No breaking changes
```

### Code Review Process

1. **Automated Checks**
   - Tests must pass
   - Code coverage must be >80%
   - No security vulnerabilities

2. **Peer Review**
   - At least one maintainer will review
   - May request changes
   - Provide constructive feedback

3. **Addressing Feedback**
   - Don't force push (keep history)
   - Add new commits addressing feedback
   - Reply to all review comments
   - Re-request review when ready

4. **Merging**
   - Will be merged by maintainer after approval
   - Uses "Squash and merge" for clean history

## Reporting Issues

### Bug Reports

Include the following:

```markdown
## Description
Clear description of the bug.

## Steps to Reproduce
1. Step 1
2. Step 2
3. Expected behavior
4. Actual behavior

## Environment
- Java version: (e.g., Java 21)
- OS: (e.g., Ubuntu 22.04)
- Maven version: (e.g., 3.9.1)

## Logs
```
Paste relevant error messages or logs
```

## Screenshots
If applicable
```

### Feature Requests

Include:

```markdown
## Description
Brief description of the feature.

## Motivation
Why is this feature needed?

## Proposed Solution
How should it work?

## Alternatives
Any alternative approaches?
```

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors.

### Our Standards

Examples of behavior that contributes to a positive environment:
- Using welcoming and inclusive language
- Being respectful of differing opinions and experiences
- Accepting constructive criticism gracefully
- Focusing on what is best for the community
- Showing empathy towards other community members

Examples of unacceptable behavior:
- The use of sexualized language or imagery
- Trolling, insulting/derogatory comments, and personal attacks
- Public or private harassment
- Publishing others' private information without permission
- Conduct which could reasonably be considered inappropriate

### Enforcement

Violations of the Code of Conduct will be handled fairly and transparently. Serious violations may result in:
- Warning
- Temporary ban
- Permanent ban from the project

## Additional Resources

- [Spring Boot Best Practices](https://spring.io/guides)
- [Clean Code by Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Maven Documentation](https://maven.apache.org/guides/)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Performance_Optimization)

## Questions?

- Open an issue with the `question` label
- Check existing documentation in `/docs`
- Review project's [Architecture Documentation](docs/architecture.md)
- Reach out to the maintainers

Thank you for contributing to the Dofus Retro Price Tracker project!

# Development Environment Setup

This guide walks you through setting up a complete development environment for the Dofus Retro Price Tracker project.

## Prerequisites

### Required Software

- **Java 21+** (OpenJDK or Oracle JDK) - LTS release
- **Maven 3.9+** - Build tool
- **Docker Desktop** - Containerization (for PostgreSQL)
- **Git** - Version control

### Optional Tools

- **IntelliJ IDEA Community/Ultimate** - IDE (recommended)
- **Eclipse IDE** - Alternative IDE
- **VS Code + Extension Pack for Java** - Lightweight alternative
- **Wireshark** - Network packet analysis
- **DBeaver Community** - Database client GUI
- **pgAdmin** - PostgreSQL web client

## Platform-Specific Installation

### Java 21 Installation

#### Ubuntu/Debian/Linux
```bash
# Update package manager
sudo apt update

# Install OpenJDK 21
sudo apt install openjdk-21-jdk openjdk-21-jre

# Verify installation
java -version
javac -version

# Output should show: openjdk version "21" ...
```

#### macOS (Homebrew)
```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install OpenJDK 21
brew install openjdk@21

# Create symlink for standard Java location
sudo ln -sfn /usr/local/opt/openjdk@21/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Verify installation
java -version
```

#### Windows

**Option 1: Using Windows Package Manager**
```powershell
winget install Eclipse.Temurin.21
```

**Option 2: Manual Download**
1. Visit [Adoptium.net](https://adoptium.net/)
2. Download OpenJDK 21 (LTS) for Windows
3. Run the installer and follow prompts
4. Set JAVA_HOME in Environment Variables:
   - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.x.x`
   - Add `%JAVA_HOME%\bin` to PATH

**Verify:**
```powershell
java -version
```

### Maven Installation

#### Ubuntu/Debian/Linux
```bash
# Install Maven
sudo apt install maven

# Verify installation
mvn -version

# Output: Apache Maven 3.9.x
```

#### macOS (Homebrew)
```bash
# Install Maven
brew install maven

# Verify installation
mvn -version
```

#### Windows

**Option 1: Using Windows Package Manager**
```powershell
winget install Apache.Maven
```

**Option 2: Manual Installation**
1. Visit [maven.apache.org](https://maven.apache.org/download.cgi)
2. Download Maven binary archive (apache-maven-3.9.x-bin.zip)
3. Extract to a directory (e.g., `C:\Program Files\apache-maven-3.9.x`)
4. Set environment variable:
   - `M2_HOME=C:\Program Files\apache-maven-3.9.x`
   - Add `%M2_HOME%\bin` to PATH

**Verify:**
```powershell
mvn -version
```

### Docker Desktop Installation

#### Ubuntu/Debian/Linux
```bash
# Install Docker
sudo apt install docker.io

# Install Docker Compose
sudo apt install docker-compose

# Add current user to docker group (avoid sudo)
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker-compose --version
```

#### macOS
```bash
# Install using Homebrew
brew install --cask docker

# Or download from https://www.docker.com/products/docker-desktop

# Start Docker Desktop and verify
docker --version
docker-compose --version
```

#### Windows

1. Download [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. Run installer (requires Windows 11 Pro/Enterprise or Windows 10 Pro with WSL2)
3. Follow installation prompts
4. Restart computer
5. Open PowerShell and verify:
```powershell
docker --version
docker-compose --version
```

## Project Setup

### 1. Clone the Repository

```bash
# Clone repository
git clone https://github.com/yourrepo/dofus-retro-tracker.git

# Navigate to project directory
cd dofus-retro-tracker

# Verify structure
ls -la
# Output:
# README.md
# pom.xml
# docker-compose.yml
# src/
# docs/
```

### 2. Configure Environment Variables

```bash
# Copy environment template (if exists)
cp .env.example .env  # Optional

# Or create .env file with defaults
cat > .env << 'EOF'
# Database Configuration
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=dofus_retro_db
DATABASE_USER=dofus
DATABASE_PASSWORD=dofus_password

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Feature Flags
PACKET_CAPTURE_ENABLED=false
GUI_AUTOMATION_ENABLED=false
EOF

# Keep .env in .gitignore (don't commit sensitive data)
cat .gitignore | grep ".env"
```

### 3. Start PostgreSQL Database

```bash
# Start containers (background)
docker-compose up -d

# Verify containers are running
docker-compose ps

# Output:
# NAME              COMMAND                 STATUS
# dofus-retro-tracker-postgres-1    "docker-entrypoint.sh postgres"  Up
# dofus-retro-tracker-pgadmin-1     "/entrypoint.sh"                 Up

# Check PostgreSQL logs
docker-compose logs postgres

# Access pgAdmin (optional)
# URL: http://localhost:5050
# Username: admin@example.com
# Password: admin
```

### 4. Verify Database Connection

```bash
# Option 1: Using psql (if installed locally)
psql -h localhost -U dofus -d dofus_retro_db

# Command should prompt for password: dofus_password
# Should connect successfully

# Option 2: Using Docker container
docker exec -it dofus-retro-tracker-postgres-1 psql \
  -U dofus -d dofus_retro_db

# Option 3: Using pgAdmin GUI
# URL: http://localhost:5050
# Create server connection to postgres:5432
```

### 5. Build the Project

```bash
# Clean and build with tests
mvn clean install

# This will:
# - Download all dependencies
# - Compile source code
# - Run all unit tests
# - Package into JAR

# Expected time: 3-5 minutes (first build longer due to dependencies)

# Build output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: X.XXXs
```

**For faster builds (skip tests initially):**
```bash
mvn clean install -DskipTests

# Later, run tests separately:
mvn test
```

### 6. Configure IDE

#### IntelliJ IDEA Setup

1. **Open Project**
   - File → Open
   - Select `pom.xml` in project root
   - Click "Open as Project"

2. **Configure Java SDK**
   - File → Project Structure
   - Project Settings → Project
   - SDK → Select Java 21 (or Add New if missing)
   - Language Level → 21

3. **Enable Annotation Processing**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"
   - Check "Obtain processors from project classpath"

4. **Install Lombok Plugin**
   - File → Settings → Plugins
   - Search "Lombok"
   - Install "Lombok" by JetBrains

5. **Enable Maven Integration**
   - View → Tool Windows → Maven
   - Click "Reload All Maven Projects" icon

#### Eclipse Setup

1. **Import Project**
   - File → Import
   - Maven → Existing Maven Projects
   - Select project root directory
   - Click Finish

2. **Configure Java 21**
   - Right-click Project → Properties
   - Java Compiler → Compiler compliance level → 21
   - JRE → Execution Environment → JavaSE-21

3. **Install Lombok**
   - Download: `mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt`
   - Find lombok JAR: `find ~/.m2 -name "lombok*.jar"`
   - Run: `java -jar lombok.jar` (GUI installer will launch)
   - Select Eclipse installation and install

4. **Update Maven Settings**
   - Right-click Project → Maven → Update Project
   - Check "Force Update of Snapshots/Releases"

#### VS Code Setup

1. **Install Extensions**
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (Pivotal)
   - Maven for Java (Microsoft)

2. **Configure JDK**
   - Command Palette (Ctrl+Shift+P)
   - "Java: Configure Runtime"
   - Select or install Java 21

3. **Open Workspace**
   - File → Open Folder
   - Select project root

4. **Trust Workspace**
   - VS Code will prompt to trust workspace
   - Click "Trust"

## Build & Run

### Building the Application

```bash
# Full build with tests
mvn clean install

# Build without tests (faster)
mvn clean package -DskipTests

# Build with code coverage
mvn clean install jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS
start target/site/jacoco/index.html # Windows
xdg-open target/site/jacoco/index.html # Linux
```

### Running the Application

#### From Command Line

```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

#### From IDE

**IntelliJ IDEA:**
1. Open `src/main/java/com/dofusretro/pricetracker/DofusRetroApplication.java`
2. Click green Run icon or press Shift+F10
3. Application starts in debug mode

**Eclipse:**
1. Open `src/main/java/com/dofusretro/pricetracker/DofusRetroApplication.java`
2. Right-click → Run As → Java Application
3. Or press Ctrl+F11

**VS Code:**
1. Command Palette → "Java: Run Spring Boot App"
2. Or Debug → Run

#### Verify Application is Running

```bash
# Health check endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" },
#     "diskSpace": { "status": "UP" }
#   }
# }

# View metrics
curl http://localhost:8080/actuator/metrics

# List available endpoints
curl http://localhost:8080/actuator
```

## Running Tests

### Unit Tests

```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=ItemServiceTest

# Run specific test method
mvn test -Dtest=ItemServiceTest#testGetItemById

# Run with output
mvn test -X  # Debug level
```

### Integration Tests

```bash
# Run all tests including integration
mvn verify

# Run only integration tests
mvn failsafe:integration-test

# Skip unit tests, run integration only
mvn verify -DskipUnitTests
```

### Code Coverage

```bash
# Generate coverage report
mvn clean install jacoco:report

# View the report
cd target/site/jacoco
# Open index.html in browser

# Coverage summary in console
mvn jacoco:report
```

## Troubleshooting

### Java Not Found

**Problem:** `java: command not found` or `'java' is not recognized`

**Solutions:**

Linux/macOS:
```bash
# Find Java installation
which java
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java
export PATH=$JAVA_HOME/bin:$PATH
```

Windows:
```powershell
# Check Java in PATH
java -version

# Set JAVA_HOME
[Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\...\jdk-21', 'User')
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Maven Build Failures

**Problem:** "Cannot find symbol" or compilation errors

```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Re-download dependencies
mvn clean install -U

# In IDE, update Maven project:
# IntelliJ: Right-click pom.xml → Maven → Reload Project
# Eclipse: Right-click Project → Maven → Update Project
```

### Database Connection Issues

**Problem:** "Connection refused" or "Cannot get JDBC Connection"

```bash
# Check if PostgreSQL container is running
docker-compose ps

# View container logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose down postgres
docker-compose up -d postgres

# Wait 10 seconds for database to be ready
sleep 10

# Test connection
docker exec dofus-retro-tracker-postgres-1 psql -U dofus -d dofus_retro_db -c "SELECT 1"
```

### Port Already in Use

**Problem:** "Address already in use" on port 8080 or 5432

```bash
# Find process using port (Linux/macOS)
lsof -i :8080
lsof -i :5432

# Kill process
kill -9 <PID>

# Or change application port in command
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Or change Docker port in docker-compose.yml
```

### Packet Capture Permissions

**Problem:** "Permission denied" for network interfaces

**Linux Solution:**
```bash
# Option 1: Set capabilities (recommended)
sudo setcap cap_net_raw,cap_net_admin=eip $(which java)

# Option 2: Run as root (not recommended)
sudo mvn spring-boot:run

# Option 3: Run with packet capture disabled
mvn spring-boot:run -Dspring-boot.run.arguments="--packet.capture.enabled=false"
```

**Windows Solution:**
- Run IDE/terminal as Administrator
- OR disable packet capture in application.yml

**macOS Solution:**
- Grant Terminal "Full Disk Access" in System Preferences → Security & Privacy
- Grant application access via Xcode: `sudo xcode-select --install`

### Lombok Compilation Issues

**Problem:** Getters/setters not recognized or "symbol not found"

**Solutions:**

IntelliJ IDEA:
- File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
- Enable annotation processing
- Invalidate Caches → Restart

Eclipse:
- Install Lombok: `java -jar ~/.m2/repository/org/projectlombok/lombok/*/lombok.jar`
- Eclipse will restart automatically
- Right-click Project → Maven → Update Project

VS Code:
- Reload window (Ctrl+Shift+P → "Reload Window")
- Verify Lombok plugin installed

### Spring Boot Won't Start

**Problem:** Application fails to start

**Debug Steps:**

```bash
# View full error logs
tail -f logs/dofus-retro-tracker.log

# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=DEBUG"

# Check Spring Boot startup configuration
cat src/main/resources/application-dev.yml
cat src/main/resources/application-prod.yml

# Verify database is running
docker-compose ps
```

## Next Steps

After successful setup:

1. **Read Documentation**
   - [Architecture Documentation](architecture.md)
   - [API Documentation](API.md) (coming soon)
   - Review [Contributing Guidelines](../CONTRIBUTING.md)

2. **Run Tests**
   - Verify all tests pass: `mvn test`
   - Check code coverage: `mvn jacoco:report`

3. **Start Development**
   - Create a feature branch: `git checkout -b feature/my-feature`
   - Make changes
   - Run tests and build: `mvn clean install`
   - Commit and create pull request

4. **Explore the Codebase**
   - Look at entity classes in `model/`
   - Review service implementations in `service/`
   - Check test examples in `src/test/`

## Helpful Commands Reference

```bash
# Build & Test
mvn clean install          # Full build with tests
mvn test                   # Run tests only
mvn package -DskipTests    # Build without tests

# Running
mvn spring-boot:run                                    # Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod   # Run with prod profile

# Database
docker-compose up -d       # Start services
docker-compose down        # Stop services
docker-compose down -v     # Stop and remove volumes
docker-compose logs -f     # Follow logs

# Debugging
mvn dependency:tree        # View dependency tree
mvn clean validate         # Validate project structure
mvn help:active-profiles   # View active profiles

# Code Quality
mvn jacoco:report          # Generate code coverage
mvn spotbugs:check         # Code quality analysis (if configured)

# IDE Commands
mvn idea:idea              # Generate IntelliJ project files
mvn eclipse:eclipse        # Generate Eclipse project files
```

## Resources & Links

- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA Docs](https://spring.io/projects/spring-data-jpa)
- [Maven Official Guide](https://maven.apache.org/guides/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Lombok Documentation](https://projectlombok.org/)

## Getting Help

If you encounter issues:

1. Check [Troubleshooting section](#troubleshooting) above
2. Review project's [GitHub Issues](https://github.com/yourrepo/issues)
3. Check [Architecture Documentation](architecture.md)
4. Ask in project's Discussions or Discord
5. Review [Contributing Guidelines](../CONTRIBUTING.md)

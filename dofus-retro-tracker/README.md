# Dofus Retro Price Tracker

[![CI/CD Pipeline](https://github.com/yourusername/dofus-retro-price-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/yourusername/dofus-retro-price-tracker/actions)
[![codecov](https://codecov.io/gh/yourusername/dofus-retro-price-tracker/branch/main/graph/badge.svg)](https://codecov.io/gh/yourusername/dofus-retro-price-tracker)

A comprehensive price tracking and analysis system for the Dofus Retro auction house (HDV). This application captures market data through network packet analysis and GUI automation, providing historical price trends and market insights.

## Features

- **Real-time Price Tracking**: Capture market data from Dofus Retro auction house
- **Historical Analysis**: Track price trends over time
- **REST API**: Query market data and statistics via RESTful endpoints
- **Automated Data Collection**: GUI automation for comprehensive market scanning
- **PostgreSQL Storage**: Reliable data persistence with full ACID compliance
- **Caching**: High-performance caching with Caffeine

## Documentation

- **[Development Setup Guide](docs/setup.md)** - Complete environment setup for all platforms
- **[Architecture Documentation](docs/architecture.md)** - System design, components, and data flow
- **[Contributing Guidelines](CONTRIBUTING.md)** - How to contribute to the project
- **[Project Structure](PROJECT_STRUCTURE.md)** - Detailed directory and module layout
- **[Protocol Analysis](docs/PROTOCOL_ANALYSIS.md)** - Dofus protocol specifications
- **[Packet Capture Setup](docs/PCAP4J_SETUP.md)** - Pcap4j configuration guide
- **[GUI Automation](docs/GUI_AUTOMATION.md)** - SikuliX automation setup

## Technology Stack

### Backend
- **Java 21** (LTS) - Latest long-term support release
- **Spring Boot 3.3.5** - Modern Java framework with auto-configuration
- **Spring Data JPA** - Data access layer with Hibernate ORM
- **PostgreSQL 16** - Enterprise-grade relational database
- **Flyway** - Database schema version control and migrations

### Network & Automation
- **Pcap4j 1.8.2** - Pure Java packet capture library (cross-platform)
- **SikuliX 2.0.5** - GUI automation with OCR capabilities
- **JavaCV 1.5.9** - Java wrapper for OpenCV (image processing)

### Utilities
- **Lombok** - Boilerplate code reduction (getters, setters, constructors)
- **Caffeine** - High-performance in-memory caching library
- **Guava 33.0.0** - Google's core utility libraries for Java

### Testing
- **JUnit 5** - Modern unit testing framework
- **Mockito** - Mocking framework for unit tests
- **AssertJ** - Fluent assertion library
- **H2** - In-memory database for integration testing

### DevOps & Monitoring
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Spring Boot Actuator** - Application monitoring and management
- **Prometheus** - Metrics collection
- **JaCoCo** - Code coverage analysis

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven 3.9+
- (Optional) libpcap/WinPcap for packet capture

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/dofus-retro-price-tracker.git
cd dofus-retro-price-tracker
```

### 2. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start:
- PostgreSQL 16 on port 5432
- (Optional) pgAdmin on port 5050

### 3. Configure Application

Copy and edit the environment file if needed:

```bash
cp .env.example .env
```

Default configuration uses:
- Database: `dofus_retro_db`
- User: `dofus`
- Password: `dofus_password` (change in production!)

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

Development mode:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Production mode:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 6. Verify Installation

Check health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Project Structure

```
dofus-retro-tracker/
├── src/main/java/com/dofusretro/pricetracker/
│   ├── DofusRetroApplication.java      # Main Spring Boot entry point
│   ├── config/                         # Spring configuration & beans
│   │   ├── CacheConfig.java
│   │   ├── DatabaseConfig.java
│   │   └── WebConfig.java
│   ├── model/                          # JPA entities
│   │   ├── Item.java
│   │   ├── SubCategory.java
│   │   └── PriceEntry.java
│   ├── repository/                     # Spring Data JPA repositories
│   │   ├── ItemRepository.java
│   │   ├── SubCategoryRepository.java
│   │   └── PriceEntryRepository.java
│   ├── service/                        # Business logic
│   │   ├── ItemPriceService.java
│   │   ├── PacketCaptureService.java
│   │   └── AutomationService.java
│   ├── controller/                     # REST API endpoints
│   │   ├── ItemController.java
│   │   └── MarketController.java
│   ├── dto/                            # Data Transfer Objects
│   │   ├── ItemDTO.java
│   │   └── PriceDTO.java
│   ├── protocol/                       # Dofus packet parsing
│   │   ├── ProtocolParser.java
│   │   └── HdvPriceMessage.java
│   ├── automation/                     # GUI automation
│   │   ├── GuiAutomationService.java
│   │   └── TemplateRegistry.java
│   └── exception/                      # Custom exceptions
│       ├── ItemNotFoundException.java
│       └── PacketParseException.java
├── src/main/resources/
│   ├── application.yml                 # Default configuration
│   ├── application-dev.yml             # Development profile
│   ├── application-prod.yml            # Production profile
│   ├── db/migration/                   # Flyway SQL migrations
│   │   ├── V1__init_schema.sql
│   │   └── V2__add_indexes.sql
│   ├── templates/                      # SikuliX GUI automation templates
│   └── logback-spring.xml              # Logging configuration
├── src/test/java/                      # Unit and integration tests
├── docs/                               # Project documentation
│   ├── architecture.md                 # System architecture
│   ├── setup.md                        # Development setup guide
│   └── API.md                          # API documentation (upcoming)
├── pom.xml                             # Maven project configuration
├── docker-compose.yml                  # Docker services
├── Dockerfile                          # Application container image
└── README.md                           # This file
```

## Configuration

### Application Profiles

- **dev**: Development profile with debug logging and H2 in-memory database support
- **prod**: Production profile with optimized settings and external configuration

### Environment Variables (Production)

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/dofus_retro_db
DB_USERNAME=dofus
DB_PASSWORD=your_secure_password
SERVER_PORT=8080
PACKET_CAPTURE_ENABLED=true
GUI_AUTOMATION_ENABLED=false
```

## Packet Capture

The application uses **Pcap4j** to capture network packets from the Dofus Retro client. This allows real-time monitoring of market data without modifying the game client.

### Prerequisites

#### Linux
- **libpcap** installed (usually pre-installed)
- **Appropriate permissions** to capture packets

```bash
# Debian/Ubuntu
sudo apt-get install libpcap-dev

# RedHat/CentOS/Fedora
sudo yum install libpcap libpcap-devel
```

#### Windows
- **Npcap** installed (download from https://npcap.com/)
- Run as Administrator OR set appropriate permissions

#### macOS
- **libpcap** (built-in, no installation needed)
- Run with sudo for packet capture

### Required Permissions

#### Linux - Recommended Method (Set Capabilities)

```bash
# Find your Java executable
which java

# Set capabilities (allows packet capture without root)
sudo setcap cap_net_raw,cap_net_admin=eip /path/to/java

# For OpenJDK 21, typically:
sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-21-openjdk-amd64/bin/java

# Verify capabilities
getcap /path/to/java
```

**Note**: Capabilities need to be reapplied after Java updates.

#### Windows

Right-click the application and select **"Run as Administrator"**.

#### macOS

```bash
sudo mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Configuration

Packet capture can be configured in `application.yml`:

```yaml
packet:
  capture:
    enabled: true                # Enable/disable packet capture
    dofus-port: 5555            # Dofus Retro server port
    network-interface: null      # Auto-detect or specify (e.g., "eth0")
    snap-len: 65536             # Maximum bytes per packet
    timeout: 1000               # Read timeout (ms)
    queue-capacity: 1000        # Packet queue size
    queue-timeout: 100          # Queue offer timeout (ms)
```

### Testing Packet Capture

Run the proof-of-concept to verify your setup:

```bash
# Linux (with capabilities set)
mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"

# Linux (with sudo if capabilities not set)
sudo mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"

# Windows (as Administrator)
mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"
```

Expected output should list your network interfaces and attempt to capture packets on port 5555.

### Monitoring Packet Capture

Check the health endpoint to verify packet capture is running:

```bash
curl http://localhost:8080/actuator/health
```

Response when packet capture is active:

```json
{
  "status": "UP",
  "components": {
    "packetCaptureHealthIndicator": {
      "status": "UP",
      "details": {
        "status": "capturing",
        "queueSize": 5,
        "queueCapacity": 1000,
        "queueUtilization": "0.5%",
        "dofusPort": 5555,
        "networkInterface": "auto-detected"
      }
    }
  }
}
```

### Troubleshooting

#### Error: "No network interfaces found"

**Cause**: Insufficient permissions or libpcap not installed.

**Solution**:
- Linux: Set capabilities or run with sudo
- Windows: Install Npcap and run as Administrator
- macOS: Run with sudo

#### Error: "Permission denied"

**Cause**: User doesn't have permission to capture packets.

**Solution**:
- Check capabilities: `getcap /path/to/java`
- Run with sudo to confirm it's a permission issue
- See [docs/PCAP4J_SETUP.md](docs/PCAP4J_SETUP.md) for detailed troubleshooting

#### Warning: "Packet queue full, dropping packets"

**Cause**: Packets arriving faster than they can be processed.

**Solution**:
- Increase `queue-capacity` in configuration
- Check if protocol parser is keeping up
- Monitor queue utilization via health endpoint

#### No Packets Being Captured

**Possible causes**:
1. Dofus client not running or not connected
2. Wrong port configured (check `dofus-port` setting)
3. Wrong network interface selected
4. BPF filter too restrictive

**Debug steps**:
1. Verify Dofus is running: Check task manager/process list
2. Check network traffic: `sudo tcpdump -i any tcp port 5555`
3. Check logs: Look for error messages in application logs
4. Run PoC: Execute Pcap4jPoC to test basic capture

For complete setup instructions, see **[docs/PCAP4J_SETUP.md](docs/PCAP4J_SETUP.md)**.

## Database Setup

### Using Docker Compose (Recommended)

```bash
# Start PostgreSQL and pgAdmin
docker-compose up -d

# View logs
docker-compose logs -f postgres

# Stop services
docker-compose down

# Stop and remove all data
docker-compose down -v
```

**Services Started:**
- PostgreSQL 16 on `localhost:5432`
- pgAdmin 4 on `http://localhost:5050` (optional)
  - Username: `admin@example.com`
  - Password: `admin`

### Manual PostgreSQL Setup

If not using Docker:

```bash
# Create database
createdb dofus_retro_db

# Create user with password
psql -c "CREATE USER dofus WITH PASSWORD 'dofus_password';"

# Grant privileges
psql -c "GRANT ALL PRIVILEGES ON DATABASE dofus_retro_db TO dofus;"

# Connect to database
psql -h localhost -U dofus -d dofus_retro_db
```

### Database Migrations with Flyway

Migrations are version-controlled SQL scripts located in `src/main/resources/db/migration/`

**Migration files:**
- `V1__init_schema.sql` - Initial schema creation
- `V2__add_indexes.sql` - Performance indexes
- Naming convention: `V{version}__{description}.sql`

**Migrations run automatically** when the application starts.

**Manual migration execution:**
```bash
# Run migrations via Maven
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Repair migration history (use with caution)
mvn flyway:repair
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run with Coverage

```bash
mvn clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

### Integration Tests

```bash
mvn verify
```

## API Endpoints

### Health Check

```
GET /actuator/health
```

### Metrics

```
GET /actuator/metrics
GET /actuator/prometheus
```

### Items (Coming in Wave 1)

```
GET /api/v1/items
GET /api/v1/items/{id}
GET /api/v1/items/{id}/prices
```

### Market Data (Coming in Wave 2)

```
GET /api/v1/market/listings
GET /api/v1/market/statistics
```

## Development

### Code Style

This project follows:
- Java Code Conventions
- Spring Boot best practices
- Clean Code principles

### Logging

Logs are written to:
- Console (development)
- `logs/dofus-retro-tracker.log` (all levels)
- `logs/dofus-retro-tracker-error.log` (errors only)

Log levels can be configured in `application.yml`

### Building Docker Image

```bash
docker build -t dofus-retro-price-tracker:latest .
```

## CI/CD

GitHub Actions workflow includes:
- Build and test on push/PR
- Code coverage reporting
- Dependency checks
- Docker image building

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps

# View PostgreSQL logs
docker-compose logs postgres

# Connect manually
psql -h localhost -U dofus -d dofus_retro_db
```

### Maven Build Issues

```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Update dependencies
mvn clean install -U
```

### Application Won't Start

1. Check logs in `logs/` directory
2. Verify database is running
3. Check configuration in `application-{profile}.yml`
4. Ensure Java 21 is being used: `java --version`

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for detailed instructions on:
- Code style and standards
- Testing requirements
- Git workflow
- Pull request process
- Reporting issues

Quick start:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes following [conventional commits](CONTRIBUTING.md#git-workflow)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request and see the [PR process](CONTRIBUTING.md#pull-request-process)

## License

This project is for educational and personal use only.

## Acknowledgments

- Original Python implementation: [HDVParserDofus2Python](https://github.com/original/repo)
- Dofus protocol definitions: [labot](https://github.com/louisabraham/labot)

## Project Status

**Current Phase**: Wave 1 - Database Schema & Documentation (IN PROGRESS)

### Wave 0 - Foundation (COMPLETED)
- ✅ Maven/Spring Boot project setup
- ✅ PostgreSQL + Docker Compose configuration
- ✅ Application configuration (dev/prod profiles)
- ✅ Logging setup (Logback)
- ✅ Package structure
- ✅ Core dependencies
- ✅ CI/CD pipeline skeleton

### Wave 1 - Database Schema & API Foundation (IN PROGRESS)
- ✅ Comprehensive documentation
  - ✅ Development setup guide (all platforms)
  - ✅ Architecture documentation
  - ✅ Contributing guidelines
- 🔄 Database schema and JPA entities
- 🔄 Service layer implementation
- 🔄 REST API endpoints
- 🔄 Unit and integration tests

### Wave 2 (PLANNED)
- Packet capture implementation
- Protocol parsing
- Price recording pipeline
- Market analysis features

### Wave 3 (PLANNED)
- GUI automation with SikuliX
- Automated market scanning
- User interface
- Authentication & authorization

### Wave 4 (PLANNED)
- Advanced analytics and predictions
- WebSocket API for real-time updates
- Mobile client support

## Contact

For questions or issues, please open an issue on GitHub.

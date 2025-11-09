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

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Database**: PostgreSQL 16
- **Build Tool**: Maven
- **Packet Capture**: pcap4j 1.8.2
- **GUI Automation**: SikuliX 2.0.5
- **Image Processing**: JavaCV 1.5.9
- **Caching**: Caffeine
- **Database Migration**: Flyway

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
src/main/java/com/dofusretro/pricetracker/
├── DofusRetroApplication.java  # Main application class
├── config/                     # Spring configuration
├── model/                      # JPA entities
├── repository/                 # Spring Data repositories
├── service/                    # Business logic
├── controller/                 # REST controllers
├── dto/                        # Data transfer objects
├── protocol/                   # Packet parsing
├── automation/                 # GUI automation
└── exception/                  # Custom exceptions
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

## Database Setup

### Using Docker Compose (Recommended)

```bash
# Start database
docker-compose up -d

# Stop database
docker-compose down

# Stop and remove data
docker-compose down -v
```

### Manual PostgreSQL Setup

```bash
# Create database
createdb dofus_retro_db

# Create user
psql -c "CREATE USER dofus WITH PASSWORD 'dofus_password';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE dofus_retro_db TO dofus;"
```

### Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`

Migrations run automatically on application startup.

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

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is for educational and personal use only.

## Acknowledgments

- Original Python implementation: [HDVParserDofus2Python](https://github.com/original/repo)
- Dofus protocol definitions: [labot](https://github.com/louisabraham/labot)

## Project Status

**Current Phase**: Wave 0 - Foundation (COMPLETED)

### Completed
- ✅ Maven/Spring Boot project setup
- ✅ PostgreSQL + Docker Compose configuration
- ✅ Application configuration (dev/prod profiles)
- ✅ Logging setup (Logback)
- ✅ Package structure
- ✅ Core dependencies
- ✅ CI/CD pipeline skeleton

### Upcoming
- 🔄 Wave 1: Database schema and entities
- 🔄 Wave 2: Packet capture implementation
- 🔄 Wave 3: GUI automation
- 🔄 Wave 4: REST API endpoints

## Contact

For questions or issues, please open an issue on GitHub.

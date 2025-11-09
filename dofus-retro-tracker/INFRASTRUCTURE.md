# Infrastructure Documentation

## Wave 2 Infrastructure Enhancement

This document describes the infrastructure components added in Wave 2 of the Dofus Retro Price Tracker project.

## Table of Contents

- [Overview](#overview)
- [Configuration Management](#configuration-management)
- [Docker Setup](#docker-setup)
- [CI/CD Pipeline](#cicd-pipeline)
- [Utility Scripts](#utility-scripts)
- [Environment Variables](#environment-variables)
- [Deployment](#deployment)

---

## Overview

Wave 2 introduces production-ready infrastructure components:

- **Advanced Configuration Management**: Type-safe, validated configuration using `@ConfigurationProperties`
- **Multi-stage Docker Builds**: Optimized production Docker images
- **Environment-specific Docker Compose**: Separate configurations for dev and prod
- **Enhanced CI/CD**: API testing, Docker builds, and improved reporting
- **Utility Scripts**: Automated setup and health checking

## Configuration Management

### Configuration Properties Classes

All configuration is externalized and validated at startup:

#### `AppProperties.java`
```java
@ConfigurationProperties(prefix = "dofus.retro.tracker")
```
Binds to application-specific settings:
- Application version
- Packet capture settings
- GUI automation settings
- Cache configuration

#### `DatabaseProperties.java`
```java
@ConfigurationProperties(prefix = "spring.datasource")
```
Database and HikariCP connection pool settings with validation:
- Pool size constraints (min/max)
- Timeout settings
- Connection lifecycle management

#### `CacheProperties.java`
```java
@ConfigurationProperties(prefix = "dofus.retro.tracker.cache")
```
Cache configuration for both Caffeine and Redis:
- TTL and size limits with validation
- Redis connection settings

### Configuration Files

| File | Purpose | Environment |
|------|---------|-------------|
| `application.yml` | Base configuration | All |
| `application-dev.yml` | Development overrides | Development |
| `application-prod.yml` | Production settings | Production |
| `application-test.yml` | Test configuration | Tests |

### Key Features

- **Validation**: `@Validated` with Jakarta Bean Validation annotations
- **Environment Variables**: All sensitive data from environment
- **Profiles**: Easy switching between dev/prod/test
- **Type Safety**: Compile-time checking of configuration

## Docker Setup

### Multi-stage Dockerfile

The production `Dockerfile` uses three stages:

1. **Dependencies Stage**: Downloads Maven dependencies (cached)
2. **Build Stage**: Compiles and packages the application
3. **Runtime Stage**: Minimal Alpine-based JRE image

**Key Features:**
- Layer caching for faster builds
- Non-root user (UID 1000)
- Health checks using actuator endpoints
- Optimized JVM settings
- Production-ready logging

### Docker Compose Files

#### `docker-compose.yml`
Base configuration with all services:
- PostgreSQL 16
- Spring Boot application
- Redis (optional, via profile)
- pgAdmin (optional, via profile)

**Services:**
```yaml
services:
  postgres:   # PostgreSQL database
  app:        # Spring Boot application
  redis:      # Redis cache (profile: redis)
  pgadmin:    # Database UI (profile: tools)
```

#### `docker-compose.dev.yml`
Development overrides:
- Debug port exposed (5005)
- Source code mounting for hot reload
- Reduced resource limits
- pgAdmin enabled by default
- No auto-restart

**Usage:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

#### `docker-compose.prod.yml`
Production overrides:
- Higher memory limits (1-3GB)
- Production JVM options
- Redis enabled by default
- Log rotation configured
- Restart policies
- Strong password requirements

**Usage:**
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up
```

### Resource Limits

| Service | Dev CPU | Dev RAM | Prod CPU | Prod RAM |
|---------|---------|---------|----------|----------|
| postgres | 1 core | 1GB | 4 cores | 2GB |
| app | 1 core | 1GB | 4 cores | 3GB |
| redis | 0.25 core | 128MB | 2 cores | 1GB |

### Health Checks

All services have health checks:
- **PostgreSQL**: `pg_isready` command
- **Application**: Actuator `/actuator/health/liveness`
- **Redis**: `redis-cli ping`

## CI/CD Pipeline

### GitHub Actions Workflow

File: `.github/workflows/ci.yml`

#### Jobs

1. **build-and-test**
   - Build with Maven
   - Run unit tests
   - Run integration tests
   - Generate code coverage
   - Publish test reports
   - Archive artifacts

2. **code-quality**
   - Maven verify
   - Checkstyle (optional)

3. **api-contract-testing**
   - Start application
   - Test actuator endpoints
   - Validate health checks
   - (Placeholder for Pact/Spring Cloud Contract)

4. **docker-build-and-push**
   - Build Docker image
   - Cache layers with GitHub Actions cache
   - Push to Docker Hub (if configured)
   - Upload image as artifact

5. **dependency-check**
   - Dependency tree
   - Vulnerability scanning
   - OWASP check (placeholder)

6. **performance-testing**
   - Placeholder for JMeter/Gatling tests

7. **notification**
   - Report workflow status

#### Triggers

- Push to: main, develop, feature/*, release/*
- Pull requests to: main, develop
- Manual workflow dispatch

#### Secrets Required

- `CODECOV_TOKEN`: For code coverage reports
- `DOCKER_USERNAME`: Docker Hub username (optional)
- `DOCKER_PASSWORD`: Docker Hub password (optional)

## Utility Scripts

### `run-dev.sh`

Starts the development environment:
```bash
./scripts/run-dev.sh
```

**Features:**
- Checks Docker is running
- Builds application
- Starts all development services
- Waits for services to be healthy
- Displays connection info
- Tails application logs

### `run-prod.sh`

Starts the production environment:
```bash
./scripts/run-prod.sh
```

**Features:**
- Validates required environment variables
- Confirms before stopping existing containers
- Pulls latest images
- Starts production services
- Runs health checks
- Displays monitoring commands

**Required Environment Variables:**
- `DB_PASSWORD`
- `POSTGRES_PASSWORD`

### `health-check.sh`

Comprehensive health check script:
```bash
./scripts/health-check.sh
```

**Checks:**
1. Docker container status
2. Database health (PostgreSQL)
3. Application health (actuator endpoints)
4. Liveness and readiness probes
5. Actuator endpoints (info, metrics, prometheus)
6. Resource usage
7. Volume status

**Output:**
- Pass/fail for each check
- Summary with counts
- Exit code 0 on success, 1 on failure

## Environment Variables

### Creating .env File

1. Copy the template:
```bash
cp .env.example .env
```

2. Edit `.env` with your values

3. **IMPORTANT**: Never commit `.env` to version control!

### Required Variables (Production)

- `POSTGRES_PASSWORD`: Database password
- `DB_PASSWORD`: Application database password (should match)

### Optional Variables

See `.env.example` for full list of configurable variables.

### Security Best Practices

1. Use strong passwords in production
2. Never commit secrets to git
3. Use environment-specific secrets
4. Rotate credentials regularly
5. Use secrets management tools (Vault, AWS Secrets Manager, etc.)

## Deployment

### Development Deployment

```bash
# 1. Clone repository
git clone <repo-url>
cd dofus-retro-tracker

# 2. Copy environment file
cp .env.example .env

# 3. Start development environment
./scripts/run-dev.sh

# 4. Access services
# - App: http://localhost:8080
# - pgAdmin: http://localhost:5050
```

### Production Deployment

```bash
# 1. Set environment variables
export POSTGRES_PASSWORD="strong_password_here"
export DB_PASSWORD="strong_password_here"

# 2. Start production environment
./scripts/run-prod.sh

# 3. Verify health
./scripts/health-check.sh

# 4. Monitor logs
docker logs -f dofus-retro-app
```

### Docker Image Build

```bash
# Build image
docker build -t dofus-retro-tracker:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  dofus-retro-tracker:latest
```

### Kubernetes Deployment (Future)

Infrastructure is ready for Kubernetes:
- Health checks (liveness/readiness)
- Resource limits defined
- 12-factor app principles
- Stateless application design
- Externalized configuration

## Monitoring

### Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Overall health status |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-format metrics |

### Logging

**Development:**
- Console output with formatted logs
- SQL logging enabled
- Debug level for application packages

**Production:**
- File-based logging with rotation
- INFO level for application
- WARN level for frameworks
- Max file size: 100MB
- Max history: 30 days
- Total cap: 3GB

### Resource Monitoring

```bash
# View container stats
docker stats

# View logs
docker logs -f dofus-retro-app

# View specific time range
docker logs --since 1h dofus-retro-app

# View with timestamps
docker logs -t dofus-retro-app
```

## Troubleshooting

### Common Issues

**1. Port already in use**
```bash
# Find process using port
lsof -i :8080

# Stop conflicting containers
docker-compose down
```

**2. Database connection fails**
```bash
# Check database is running
docker ps | grep postgres

# Check database logs
docker logs dofus-retro-db

# Test connection
docker exec -it dofus-retro-db psql -U dofus -d dofus_retro_db
```

**3. Application won't start**
```bash
# Check application logs
docker logs dofus-retro-app

# Check environment variables
docker exec dofus-retro-app env

# Verify configuration
docker exec dofus-retro-app cat /app/logs/application.log
```

**4. Health checks failing**
```bash
# Run comprehensive health check
./scripts/health-check.sh

# Test endpoints manually
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
```

### Clean Slate

To start fresh:
```bash
# Stop and remove containers
docker-compose down

# Remove volumes (WARNING: deletes data!)
docker volume rm $(docker volume ls -q | grep dofus-retro)

# Remove images
docker rmi dofus-retro-tracker:latest

# Restart
./scripts/run-dev.sh
```

## Performance Tuning

### JVM Options

**Development:**
```bash
-Xms256m -Xmx512m -XX:+UseG1GC
```

**Production:**
```bash
-Xms1024m -Xmx2048m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
-XX:+UseStringDeduplication
-XX:+ParallelRefProcEnabled
```

### Database Connection Pool

**Development:**
- Max: 5
- Min Idle: 2

**Production:**
- Max: 20
- Min Idle: 10
- Leak Detection: 60s

### Cache Settings

**Development:**
- Type: Caffeine
- TTL: 600s (10 min)
- Max Size: 500

**Production:**
- Type: Caffeine or Redis
- TTL: 3600s (1 hour)
- Max Size: 10,000

## Next Steps

- Configure Docker registry for image distribution
- Set up Kubernetes manifests
- Configure production secrets management
- Set up APM (Application Performance Monitoring)
- Configure centralized logging (ELK stack)
- Set up alerting (Prometheus + Alertmanager)

---

**Author:** AGENT-INFRA
**Wave:** 2 - Business Logic & REST API
**Date:** 2025-11-09
**Version:** 0.1.0

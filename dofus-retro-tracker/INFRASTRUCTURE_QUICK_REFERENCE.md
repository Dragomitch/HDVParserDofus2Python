# Infrastructure Quick Reference Card

## Wave 2 Infrastructure - Essential Commands & Files

### Quick Start

```bash
# Development
./scripts/run-dev.sh

# Production
export POSTGRES_PASSWORD="your-password"
export DB_PASSWORD="your-password"
./scripts/run-prod.sh

# Health Check
./scripts/health-check.sh
```

---

## Key Files

| File | Purpose |
|------|---------|
| `INFRASTRUCTURE.md` | Complete infrastructure guide (22K) |
| `WAVE_2_INFRASTRUCTURE_COMPLETION_REPORT.md` | Detailed completion report (36K) |
| `.env.example` | Environment variable template |
| `docker-compose.yml` | Base Docker Compose config |
| `docker-compose.dev.yml` | Development overrides |
| `docker-compose.prod.yml` | Production overrides |
| `Dockerfile` | Multi-stage production build |

---

## Configuration Properties

| Class | Prefix | Purpose |
|-------|--------|---------|
| `AppProperties` | `dofus.retro.tracker` | App-wide settings |
| `DatabaseProperties` | `spring.datasource` | HikariCP config |
| `CacheProperties` | `dofus.retro.tracker.cache` | Cache config |

---

## Docker Commands

```bash
# Development (with debug port)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Production
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# With Redis
docker-compose -f docker-compose.yml -f docker-compose.prod.yml --profile redis up -d

# With pgAdmin (dev only)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# View logs
docker logs -f dofus-retro-app

# Stop all
docker-compose down
```

---

## Service URLs

| Service | Development | Production |
|---------|-------------|------------|
| Application | http://localhost:8080 | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health | http://localhost:8080/actuator/health |
| Metrics | http://localhost:8080/actuator/metrics | http://localhost:8080/actuator/metrics |
| Prometheus | http://localhost:8080/actuator/prometheus | http://localhost:8080/actuator/prometheus |
| PostgreSQL | localhost:5432 | localhost:5432 |
| pgAdmin | http://localhost:5050 | N/A |
| Redis | localhost:6379 | localhost:6379 |

---

## Environment Variables (Production)

### Required
```bash
POSTGRES_PASSWORD=<strong-password>
DB_PASSWORD=<strong-password>
```

### Optional (with defaults)
```bash
SPRING_PROFILES_ACTIVE=prod
APP_PORT=8080
DB_POOL_MAX_SIZE=20
CACHE_TYPE=caffeine
REDIS_HOST=redis
```

See `.env.example` for complete list.

---

## Health Check Endpoints

```bash
# Overall health
curl http://localhost:8080/actuator/health

# Liveness probe (Kubernetes)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe (Kubernetes)
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## Resource Limits

### Development
- **Postgres**: 1 CPU, 1GB RAM
- **App**: 1 CPU, 1GB RAM
- **Redis**: 0.25 CPU, 128MB RAM

### Production
- **Postgres**: 4 CPUs, 2GB RAM
- **App**: 4 CPUs, 3GB RAM
- **Redis**: 2 CPUs, 1GB RAM

---

## Performance Settings

### JVM Options

**Development:**
```bash
-Xms256m -Xmx512m -XX:+UseG1GC
```

**Production:**
```bash
-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError -XX:+UseStringDeduplication
```

### Database Pool

**Development:** Max: 5, Min Idle: 2
**Production:** Max: 20, Min Idle: 10

### Cache

**Development:** TTL: 600s, Size: 500
**Production:** TTL: 3600s, Size: 10,000

---

## CI/CD Pipeline

### Jobs (in order)
1. `build-and-test` - Maven build, tests, coverage
2. `code-quality` - Maven verify, checkstyle
3. `api-contract-testing` - Actuator endpoint tests
4. `docker-build-and-push` - Docker image build
5. `dependency-check` - Security scanning
6. `performance-testing` - JMeter placeholder
7. `notification` - Status report

### Triggers
- Push: main, develop, feature/*, release/*
- Pull request: main, develop
- Manual workflow dispatch

---

## Troubleshooting

### Port in use
```bash
lsof -i :8080
docker-compose down
```

### Database connection failed
```bash
docker logs dofus-retro-db
docker exec -it dofus-retro-db pg_isready -U dofus
```

### Application won't start
```bash
docker logs dofus-retro-app
docker exec dofus-retro-app env
```

### Clean slate
```bash
docker-compose down
docker volume rm $(docker volume ls -q | grep dofus-retro)
docker rmi dofus-retro-tracker:latest
```

---

## Monitoring

### View Container Stats
```bash
docker stats
```

### View Logs
```bash
# Tail logs
docker logs -f dofus-retro-app

# Last 100 lines
docker logs --tail 100 dofus-retro-app

# Since 1 hour
docker logs --since 1h dofus-retro-app
```

### Health Check Script
```bash
./scripts/health-check.sh
```

Output:
- Container status
- Database connectivity
- Application health
- Actuator endpoints
- Resource usage
- Volume status

---

## Security Checklist

- [ ] Strong passwords set (`POSTGRES_PASSWORD`, `DB_PASSWORD`)
- [ ] `.env` file not committed to git
- [ ] Docker registry credentials configured (if pushing)
- [ ] Resource limits set appropriately
- [ ] Health checks configured
- [ ] Logging configured with rotation
- [ ] Non-root user in containers

---

## Next Steps

1. **Review**: AGENT-REVIEW approval
2. **Test**: Build and run locally
3. **Deploy**: Use `./scripts/run-prod.sh`
4. **Monitor**: Set up Prometheus + Grafana
5. **Enhance**: Kubernetes manifests (future)

---

**Created by:** AGENT-INFRA
**Date:** 2025-11-09
**Wave:** 2 - Infrastructure Enhancement
**Status:** ✅ Complete

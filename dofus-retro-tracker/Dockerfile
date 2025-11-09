# Multi-stage build for Dofus Retro Price Tracker
# Wave 2 - Production-optimized Docker image

# =============================================================================
# Stage 1: Build Dependencies (cached layer)
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS dependencies

WORKDIR /build

# Copy only pom.xml to cache dependencies
COPY pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# =============================================================================
# Stage 2: Build Application
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copy dependencies from previous stage
COPY --from=dependencies /root/.m2 /root/.m2

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build application (skip tests - they run in CI/CD)
RUN mvn clean package -DskipTests -B

# Verify JAR exists
RUN ls -lh /build/target/*.jar

# =============================================================================
# Stage 3: Runtime (Production)
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="dofus-retro-dev-team"
LABEL description="Dofus Retro Price Tracker - Wave 2"
LABEL version="0.1.0"

WORKDIR /app

# Install runtime dependencies
# - libpcap: For packet capture functionality
# - curl: For health checks
# - tzdata: For timezone support
RUN apk add --no-cache \
    libpcap-dev \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Create non-root user and group
RUN addgroup -g 1000 dofus && \
    adduser -u 1000 -G dofus -s /bin/sh -D dofus

# Create necessary directories
RUN mkdir -p /app/logs /app/data /app/tmp && \
    chown -R dofus:dofus /app

# Copy JAR from build stage
COPY --from=build --chown=dofus:dofus /build/target/*.jar /app/app.jar

# Switch to non-root user
USER dofus

# Expose application port
EXPOSE 8080

# Health check using actuator endpoint
HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=60s \
            --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Set default environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs" \
    TZ=UTC

# Set JVM DNS cache
ENV JAVA_TOOL_OPTIONS="-Dnetworkaddress.cache.ttl=60"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

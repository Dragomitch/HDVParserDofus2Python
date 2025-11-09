#!/bin/bash

################################################################################
# Development Environment Startup Script
# Dofus Retro Price Tracker - Wave 2
#
# This script starts the development environment using Docker Compose.
# It includes the database, application, and optional pgAdmin.
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${BLUE}=================================${NC}"
echo -e "${BLUE}Dofus Retro Price Tracker${NC}"
echo -e "${BLUE}Development Environment Setup${NC}"
echo -e "${BLUE}=================================${NC}\n"

# Navigate to project root
cd "$PROJECT_ROOT"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running${NC}"
    echo "Please start Docker and try again"
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Warning: docker-compose not found, using 'docker compose'${NC}"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Load environment variables from .env if it exists
if [ -f .env ]; then
    echo -e "${GREEN}Loading environment variables from .env${NC}"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}Warning: .env file not found, using defaults${NC}"
fi

# Build the application first
echo -e "\n${BLUE}Building application...${NC}"
if command -v mvn &> /dev/null; then
    mvn clean package -DskipTests
else
    echo -e "${YELLOW}Maven not found locally, will build in Docker${NC}"
fi

# Stop any existing containers
echo -e "\n${BLUE}Stopping any existing containers...${NC}"
$DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.dev.yml down

# Start services
echo -e "\n${BLUE}Starting development services...${NC}"
$DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.dev.yml up -d --build

# Wait for services to be healthy
echo -e "\n${BLUE}Waiting for services to be ready...${NC}"

# Wait for PostgreSQL
echo -n "Waiting for PostgreSQL..."
for i in {1..30}; do
    if docker exec dofus-retro-db pg_isready -U dofus > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# Wait for Application
echo -n "Waiting for Application..."
for i in {1..60}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# Display service information
echo -e "\n${GREEN}=================================${NC}"
echo -e "${GREEN}Services are ready!${NC}"
echo -e "${GREEN}=================================${NC}\n"

echo -e "${BLUE}Service URLs:${NC}"
echo -e "  Application:  ${GREEN}http://localhost:8080${NC}"
echo -e "  Health Check: ${GREEN}http://localhost:8080/actuator/health${NC}"
echo -e "  API Docs:     ${GREEN}http://localhost:8080/swagger-ui.html${NC} (if enabled)"
echo -e "  PostgreSQL:   ${GREEN}localhost:5432${NC}"
echo -e "  pgAdmin:      ${GREEN}http://localhost:5050${NC} (if started with --profile tools)"

echo -e "\n${BLUE}Database Connection:${NC}"
echo -e "  Host:     localhost"
echo -e "  Port:     5432"
echo -e "  Database: dofus_retro_db"
echo -e "  Username: dofus"
echo -e "  Password: dofus_password"

echo -e "\n${BLUE}Useful Commands:${NC}"
echo -e "  View logs:           ${YELLOW}docker logs -f dofus-retro-app${NC}"
echo -e "  Stop services:       ${YELLOW}docker-compose -f docker-compose.yml -f docker-compose.dev.yml down${NC}"
echo -e "  Restart app:         ${YELLOW}docker-compose -f docker-compose.yml -f docker-compose.dev.yml restart app${NC}"
echo -e "  Shell into app:      ${YELLOW}docker exec -it dofus-retro-app sh${NC}"
echo -e "  Database shell:      ${YELLOW}docker exec -it dofus-retro-db psql -U dofus -d dofus_retro_db${NC}"
echo -e "  View health:         ${YELLOW}curl http://localhost:8080/actuator/health | jq${NC}"

echo -e "\n${GREEN}Development environment is ready!${NC}"
echo -e "Press Ctrl+C to stop tailing logs, or run: ${YELLOW}docker logs -f dofus-retro-app${NC}\n"

# Tail application logs
docker logs -f dofus-retro-app

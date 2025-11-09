#!/bin/bash

################################################################################
# Production Environment Startup Script
# Dofus Retro Price Tracker - Wave 2
#
# This script starts the production environment using Docker Compose.
# It requires proper environment variables to be set.
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
echo -e "${BLUE}Production Environment Setup${NC}"
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
    echo -e "${RED}Error: .env file not found${NC}"
    echo "Please create a .env file with required production variables"
    exit 1
fi

# Verify critical environment variables
REQUIRED_VARS=("DB_PASSWORD" "POSTGRES_PASSWORD")
MISSING_VARS=()

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo -e "${RED}Error: Missing required environment variables:${NC}"
    for var in "${MISSING_VARS[@]}"; do
        echo -e "  - $var"
    done
    echo -e "\nPlease set these variables in your .env file"
    exit 1
fi

echo -e "${GREEN}All required environment variables are set${NC}\n"

# Build the application
echo -e "${BLUE}Building application...${NC}"
if command -v mvn &> /dev/null; then
    mvn clean package -DskipTests
else
    echo -e "${YELLOW}Maven not found locally, will build in Docker${NC}"
fi

# Pull latest images
echo -e "\n${BLUE}Pulling latest Docker images...${NC}"
$DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.prod.yml pull

# Stop any existing containers (with confirmation)
if [ "$($DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.prod.yml ps -q)" ]; then
    echo -e "\n${YELLOW}Existing containers found${NC}"
    read -p "Do you want to stop and replace them? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Stopping existing containers...${NC}"
        $DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.prod.yml down
    else
        echo -e "${RED}Cancelled${NC}"
        exit 0
    fi
fi

# Start services
echo -e "\n${BLUE}Starting production services...${NC}"
$DOCKER_COMPOSE -f docker-compose.yml -f docker-compose.prod.yml up -d --build

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

# Wait for Redis (if enabled)
if docker ps --format '{{.Names}}' | grep -q dofus-retro-redis; then
    echo -n "Waiting for Redis..."
    for i in {1..30}; do
        if docker exec dofus-retro-redis redis-cli ping > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            break
        fi
        echo -n "."
        sleep 1
    done
fi

# Wait for Application
echo -n "Waiting for Application..."
for i in {1..90}; do
    if curl -f http://localhost:${APP_PORT:-8080}/actuator/health > /dev/null 2>&1; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# Run health check
echo -e "\n${BLUE}Running health checks...${NC}"
bash "$SCRIPT_DIR/health-check.sh"

# Display service information
echo -e "\n${GREEN}=================================${NC}"
echo -e "${GREEN}Production environment is ready!${NC}"
echo -e "${GREEN}=================================${NC}\n"

echo -e "${BLUE}Service Information:${NC}"
echo -e "  Application:  ${GREEN}http://localhost:${APP_PORT:-8080}${NC}"
echo -e "  Health Check: ${GREEN}http://localhost:${APP_PORT:-8080}/actuator/health${NC}"

echo -e "\n${BLUE}Monitoring:${NC}"
echo -e "  View logs:           ${YELLOW}docker logs -f dofus-retro-app${NC}"
echo -e "  View all services:   ${YELLOW}docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps${NC}"
echo -e "  Stop services:       ${YELLOW}docker-compose -f docker-compose.yml -f docker-compose.prod.yml down${NC}"
echo -e "  Health check:        ${YELLOW}bash scripts/health-check.sh${NC}"

echo -e "\n${YELLOW}Note: Production logs are persisted in Docker volumes${NC}"
echo -e "${YELLOW}Use 'docker volume ls' to view volumes${NC}\n"

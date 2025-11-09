#!/bin/bash

################################################################################
# Health Check Script
# Dofus Retro Price Tracker - Wave 2
#
# This script verifies that all services are healthy and responding correctly.
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

# Configuration
APP_HOST="${APP_HOST:-localhost}"
APP_PORT="${APP_PORT:-8080}"
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"

# Counters
PASSED=0
FAILED=0
WARNINGS=0

echo -e "${BLUE}=================================${NC}"
echo -e "${BLUE}Health Check Report${NC}"
echo -e "${BLUE}=================================${NC}\n"

# Function to check service
check_service() {
    local name=$1
    local check_cmd=$2

    echo -n "Checking $name... "

    if eval "$check_cmd" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}"
        ((FAILED++))
        return 1
    fi
}

# Function to check HTTP endpoint
check_http() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}

    echo -n "Checking $name... "

    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

    if [ "$response" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $response)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $response, expected $expected_status)"
        ((FAILED++))
        return 1
    fi
}

# Function to check HTTP endpoint with JSON response
check_http_json() {
    local name=$1
    local url=$2
    local json_path=$3
    local expected_value=$4

    echo -n "Checking $name... "

    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}⚠ SKIP${NC} (jq not installed)"
        ((WARNINGS++))
        return 0
    fi

    response=$(curl -s "$url" 2>/dev/null)
    actual_value=$(echo "$response" | jq -r "$json_path" 2>/dev/null)

    if [ "$actual_value" == "$expected_value" ]; then
        echo -e "${GREEN}✓ PASS${NC} ($json_path = $expected_value)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} ($json_path = $actual_value, expected $expected_value)"
        ((FAILED++))
        return 1
    fi
}

echo -e "${BLUE}1. Docker Container Status${NC}"
echo "-----------------------------------"

# Check if containers are running
if docker ps --format '{{.Names}}' | grep -q dofus-retro-app; then
    check_service "Application Container" "docker ps --format '{{.Names}}' | grep -q dofus-retro-app"
else
    echo -e "${RED}✗ Application container not running${NC}"
    ((FAILED++))
fi

if docker ps --format '{{.Names}}' | grep -q dofus-retro-db; then
    check_service "Database Container" "docker ps --format '{{.Names}}' | grep -q dofus-retro-db"
else
    echo -e "${RED}✗ Database container not running${NC}"
    ((FAILED++))
fi

if docker ps --format '{{.Names}}' | grep -q dofus-retro-redis; then
    check_service "Redis Container" "docker ps --format '{{.Names}}' | grep -q dofus-retro-redis"
else
    echo -e "${YELLOW}⚠ Redis container not running (optional)${NC}"
    ((WARNINGS++))
fi

echo -e "\n${BLUE}2. Database Health${NC}"
echo "-----------------------------------"

check_service "PostgreSQL Connection" "docker exec dofus-retro-db pg_isready -U dofus"
check_service "Database Existence" "docker exec dofus-retro-db psql -U dofus -d dofus_retro_db -c 'SELECT 1'"

echo -e "\n${BLUE}3. Application Health${NC}"
echo "-----------------------------------"

check_http "Application Health Endpoint" "http://${APP_HOST}:${APP_PORT}/actuator/health" "200"
check_http_json "Application Liveness" "http://${APP_HOST}:${APP_PORT}/actuator/health/liveness" ".status" "UP"
check_http_json "Application Readiness" "http://${APP_HOST}:${APP_PORT}/actuator/health/readiness" ".status" "UP"

echo -e "\n${BLUE}4. Actuator Endpoints${NC}"
echo "-----------------------------------"

check_http "Info Endpoint" "http://${APP_HOST}:${APP_PORT}/actuator/info" "200"
check_http "Metrics Endpoint" "http://${APP_HOST}:${APP_PORT}/actuator/metrics" "200"

if curl -s "http://${APP_HOST}:${APP_PORT}/actuator/prometheus" > /dev/null 2>&1; then
    check_http "Prometheus Metrics" "http://${APP_HOST}:${APP_PORT}/actuator/prometheus" "200"
else
    echo -e "${YELLOW}⚠ Prometheus endpoint not available (optional)${NC}"
    ((WARNINGS++))
fi

echo -e "\n${BLUE}5. Resource Usage${NC}"
echo "-----------------------------------"

# Check container resource usage
if command -v docker &> /dev/null; then
    echo -e "${BLUE}Container Resources:${NC}"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | grep dofus-retro || true
fi

echo -e "\n${BLUE}6. Volume Status${NC}"
echo "-----------------------------------"

# Check if volumes exist
if docker volume ls | grep -q dofus-retro-postgres-data; then
    echo -e "${GREEN}✓${NC} PostgreSQL data volume exists"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} PostgreSQL data volume missing"
    ((FAILED++))
fi

if docker volume ls | grep -q dofus-retro-app-logs; then
    echo -e "${GREEN}✓${NC} Application logs volume exists"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠${NC} Application logs volume missing"
    ((WARNINGS++))
fi

# Summary
echo -e "\n${BLUE}=================================${NC}"
echo -e "${BLUE}Health Check Summary${NC}"
echo -e "${BLUE}=================================${NC}"
echo -e "Passed:   ${GREEN}$PASSED${NC}"
echo -e "Failed:   ${RED}$FAILED${NC}"
echo -e "Warnings: ${YELLOW}$WARNINGS${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}All critical health checks passed!${NC}"
    exit 0
else
    echo -e "\n${RED}Some health checks failed. Please investigate.${NC}"
    exit 1
fi

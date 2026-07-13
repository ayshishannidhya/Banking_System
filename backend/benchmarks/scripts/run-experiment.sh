#!/bin/bash
# ==============================================================================
# Experiment Automation Script
# Project: Neptune Bank — IEEE Research
# Author: Ayshi Shannidhya Panda
#
# This script orchestrates a full benchmarking experiment:
#   1. Starts the appropriate Docker Compose stack
#   2. Waits for service health
#   3. Runs a warm-up phase
#   4. Executes the JMeter load test
#   5. Collects Prometheus metrics
#   6. Stops the stack
#   7. Saves results
#
# Usage:
#   ./run-experiment.sh <paradigm> <scenario> <users> <run_number>
#   ./run-experiment.sh kafka fund_transfer 5000 1
#
# Prerequisites:
#   - Docker and Docker Compose installed
#   - JMeter installed and in PATH
#   - curl installed
# ==============================================================================

set -euo pipefail

# ===========================
# CONFIGURATION
# ===========================
PARADIGM="${1:?Usage: $0 <paradigm> <scenario> <users> <run_number>}"
SCENARIO="${2:?Usage: $0 <paradigm> <scenario> <users> <run_number>}"
USERS="${3:?Usage: $0 <paradigm> <scenario> <users> <run_number>}"
RUN_NUMBER="${4:?Usage: $0 <paradigm> <scenario> <users> <run_number>}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
RESULTS_DIR="$PROJECT_DIR/benchmarks/results"
JMETER_DIR="$PROJECT_DIR/benchmarks/jmeter"

WARMUP_DURATION=60        # seconds
TEST_DURATION=300         # seconds (5 minutes)
RAMP_UP_TIME=60           # seconds
HEALTH_CHECK_RETRIES=30
HEALTH_CHECK_INTERVAL=10  # seconds

# Output file naming
RESULT_FILE="${PARADIGM}_${SCENARIO}_${USERS}_run${RUN_NUMBER}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo " Neptune Bank — IEEE Experiment Runner"
echo "=========================================="
echo " Paradigm:    $PARADIGM"
echo " Scenario:    $SCENARIO"
echo " Users:       $USERS"
echo " Run:         $RUN_NUMBER"
echo " Timestamp:   $TIMESTAMP"
echo "=========================================="

# ===========================
# FUNCTIONS
# ===========================

cleanup() {
    echo ""
    echo "[CLEANUP] Stopping Docker Compose stack..."
    cd "$PROJECT_DIR"
    docker-compose -f docker-compose.yml -f "docker-compose.${PARADIGM}.yml" down --remove-orphans 2>/dev/null || true
    echo "[CLEANUP] Done."
}

wait_for_health() {
    local service_url="$1"
    local service_name="$2"
    local retries=$HEALTH_CHECK_RETRIES

    echo "[HEALTH] Waiting for $service_name at $service_url..."
    while [ $retries -gt 0 ]; do
        if curl -sf "$service_url" > /dev/null 2>&1; then
            echo "[HEALTH] $service_name is UP"
            return 0
        fi
        retries=$((retries - 1))
        echo "[HEALTH] $service_name not ready. Retrying in ${HEALTH_CHECK_INTERVAL}s... ($retries left)"
        sleep $HEALTH_CHECK_INTERVAL
    done

    echo "[HEALTH] ERROR: $service_name failed to start"
    return 1
}

collect_prometheus_metrics() {
    local metric_name="$1"
    local output_file="$2"

    echo "[METRICS] Collecting $metric_name from Prometheus..."
    curl -sf "http://localhost:9090/api/v1/query?query=${metric_name}" \
        | python3 -m json.tool > "$output_file" 2>/dev/null || \
        echo "[METRICS] WARNING: Failed to collect $metric_name"
}

# Trap for cleanup on exit
trap cleanup EXIT

# ===========================
# STEP 1: Create results directory
# ===========================
mkdir -p "$RESULTS_DIR/$TIMESTAMP"

# ===========================
# STEP 2: Start Docker stack
# ===========================
echo ""
echo "[STEP 1/6] Starting Docker Compose stack ($PARADIGM mode)..."
cd "$PROJECT_DIR"
docker-compose -f docker-compose.yml -f "docker-compose.${PARADIGM}.yml" up -d --build

# ===========================
# STEP 3: Wait for services to be healthy
# ===========================
echo ""
echo "[STEP 2/6] Waiting for services to become healthy..."
wait_for_health "http://localhost:8080/actuator/health" "User Service"
wait_for_health "http://localhost:8083/actuator/health" "Account Service"
wait_for_health "http://localhost:8084/actuator/health" "Transaction Service"
wait_for_health "http://localhost:8086/actuator/health" "Auth Service"
wait_for_health "http://localhost:9090/-/healthy" "Prometheus"

echo "[HEALTH] All services are healthy."

# ===========================
# STEP 4: Warm-up phase
# ===========================
echo ""
echo "[STEP 3/6] Running warm-up phase (${WARMUP_DURATION}s at 50% load)..."
WARMUP_USERS=$((USERS / 2))
if [ $WARMUP_USERS -lt 10 ]; then
    WARMUP_USERS=10
fi

jmeter -n \
    -t "$JMETER_DIR/${SCENARIO}.jmx" \
    -Jusers=$WARMUP_USERS \
    -Jduration=$WARMUP_DURATION \
    -Jrampup=30 \
    -l "$RESULTS_DIR/$TIMESTAMP/warmup_${RESULT_FILE}.csv" \
    2>&1 | tail -5

echo "[WARMUP] Warm-up complete. Discarding warm-up data."
sleep 10  # Brief pause before measurement

# ===========================
# STEP 5: Run load test
# ===========================
echo ""
echo "[STEP 4/6] Running load test: $USERS users for ${TEST_DURATION}s..."

jmeter -n \
    -t "$JMETER_DIR/${SCENARIO}.jmx" \
    -Jusers=$USERS \
    -Jduration=$TEST_DURATION \
    -Jrampup=$RAMP_UP_TIME \
    -l "$RESULTS_DIR/$TIMESTAMP/${RESULT_FILE}.csv" \
    -e -o "$RESULTS_DIR/$TIMESTAMP/${RESULT_FILE}_report" \
    2>&1 | tail -20

echo "[JMETER] Load test complete."

# ===========================
# STEP 6: Collect metrics
# ===========================
echo ""
echo "[STEP 5/6] Collecting Prometheus metrics..."

METRICS_DIR="$RESULTS_DIR/$TIMESTAMP/metrics"
mkdir -p "$METRICS_DIR"

# Collect key metrics
collect_prometheus_metrics "neptune_communication_latency_seconds_count" "$METRICS_DIR/comm_latency_count.json"
collect_prometheus_metrics "neptune_communication_latency_seconds_sum" "$METRICS_DIR/comm_latency_sum.json"
collect_prometheus_metrics "neptune_communication_errors_total" "$METRICS_DIR/comm_errors.json"
collect_prometheus_metrics "process_cpu_usage" "$METRICS_DIR/cpu_usage.json"
collect_prometheus_metrics "jvm_memory_used_bytes" "$METRICS_DIR/memory_usage.json"
collect_prometheus_metrics "http_server_requests_seconds_count" "$METRICS_DIR/http_request_count.json"
collect_prometheus_metrics "http_server_requests_seconds_sum" "$METRICS_DIR/http_request_sum.json"

# Collect container-level metrics from cAdvisor
collect_prometheus_metrics 'container_cpu_usage_seconds_total{name=~"neptune.*"}' "$METRICS_DIR/container_cpu.json"
collect_prometheus_metrics 'container_memory_usage_bytes{name=~"neptune.*"}' "$METRICS_DIR/container_memory.json"

echo "[METRICS] Metrics collection complete."

# ===========================
# STEP 7: Record Docker resource usage
# ===========================
echo ""
echo "[STEP 6/6] Recording container resource snapshot..."
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}" \
    > "$RESULTS_DIR/$TIMESTAMP/${RESULT_FILE}_docker_stats.txt" 2>/dev/null || true

# ===========================
# SUMMARY
# ===========================
echo ""
echo "=========================================="
echo " EXPERIMENT COMPLETE"
echo "=========================================="
echo " Results:     $RESULTS_DIR/$TIMESTAMP/"
echo " JMeter CSV:  ${RESULT_FILE}.csv"
echo " JMeter HTML: ${RESULT_FILE}_report/"
echo " Metrics:     metrics/"
echo "=========================================="
echo ""
echo " Next steps:"
echo "   1. Copy ${RESULT_FILE}.csv to benchmarks/results/"
echo "   2. Repeat: ./run-experiment.sh $PARADIGM $SCENARIO $USERS $((RUN_NUMBER + 1))"
echo "   3. After $((5)) runs, analyze: python3 analyze_results.py --input-dir results/ --output-dir analysis/"
echo ""

#!/bin/bash
# ==============================================================================
# Chaos Engineering — Fault Injection Script
# Project: Neptune Bank — IEEE Research
# Author: Ayshi Shannidhya Panda
#
# This script injects faults for fault tolerance experiments (F1–F5).
#
# Usage:
#   ./fault-injection.sh <fault_type> [options]
#
# Fault Types:
#   kafka_crash       - Kill Kafka broker (F1)
#   service_crash     - Kill a service instance (F2)
#   db_slowdown       - Inject database latency (F3)
#   rabbitmq_restart  - Restart RabbitMQ broker (F4)
#   consumer_crash    - Kill Kafka consumers (F5)
# ==============================================================================

set -euo pipefail

FAULT_TYPE="${1:?Usage: $0 <fault_type> [container_name]}"
CONTAINER="${2:-}"
RECOVERY_WAIT=60  # seconds to wait for recovery measurement

echo "=========================================="
echo " CHAOS ENGINEERING — FAULT INJECTION"
echo "=========================================="
echo " Fault Type:  $FAULT_TYPE"
echo " Timestamp:   $(date -Iseconds)"
echo "=========================================="

case "$FAULT_TYPE" in

  kafka_crash)
    # F1: Kill Kafka broker process during active load
    echo "[F1] Killing Kafka broker..."
    CONTAINER="${CONTAINER:-neptune-kafka}"
    
    echo "[F1] Recording pre-crash metrics..."
    docker stats --no-stream "$CONTAINER" 2>/dev/null || true
    
    CRASH_TIME=$(date +%s%N)
    docker kill "$CONTAINER"
    echo "[F1] Kafka broker killed at $(date -Iseconds)"
    echo "[F1] Crash timestamp (ns): $CRASH_TIME"
    
    echo "[F1] Waiting ${RECOVERY_WAIT}s for recovery measurement..."
    sleep 10
    
    echo "[F1] Restarting Kafka broker..."
    docker start "$CONTAINER"
    RESTART_TIME=$(date +%s%N)
    echo "[F1] Kafka broker restarted at $(date -Iseconds)"
    echo "[F1] Restart timestamp (ns): $RESTART_TIME"
    
    RECOVERY_MS=$(( (RESTART_TIME - CRASH_TIME) / 1000000 ))
    echo "[F1] Downtime: ${RECOVERY_MS}ms"
    
    # Wait for full recovery
    sleep "$RECOVERY_WAIT"
    echo "[F1] Post-recovery check:"
    docker exec "$CONTAINER" /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null && \
      echo "[F1] Kafka is fully recovered" || echo "[F1] WARNING: Kafka may still be recovering"
    ;;

  service_crash)
    # F2: Kill a service instance during active load
    CONTAINER="${CONTAINER:-neptune-transaction-service}"
    echo "[F2] Killing service: $CONTAINER"
    
    CRASH_TIME=$(date +%s%N)
    docker kill "$CONTAINER"
    echo "[F2] Service killed at $(date -Iseconds)"
    
    sleep 10
    
    echo "[F2] Restarting service..."
    docker start "$CONTAINER"
    RESTART_TIME=$(date +%s%N)
    
    # Wait for health check
    echo "[F2] Waiting for health check..."
    for i in $(seq 1 30); do
      if curl -sf "http://localhost:8084/actuator/health" > /dev/null 2>&1; then
        HEALTHY_TIME=$(date +%s%N)
        MTTR=$(( (HEALTHY_TIME - CRASH_TIME) / 1000000 ))
        echo "[F2] Service recovered. MTTR: ${MTTR}ms"
        break
      fi
      sleep 2
    done
    ;;

  db_slowdown)
    # F3: Inject latency into database connections using tc netem
    CONTAINER="${CONTAINER:-neptune-postgres-transaction}"
    DELAY_MS="${3:-200}"
    DURATION="${4:-120}"
    
    echo "[F3] Injecting ${DELAY_MS}ms latency into $CONTAINER for ${DURATION}s..."
    
    docker exec "$CONTAINER" sh -c "apk add --no-cache iproute2 2>/dev/null; tc qdisc add dev eth0 root netem delay ${DELAY_MS}ms 20ms distribution normal" || \
      echo "[F3] WARNING: Could not inject latency (tc may not be available)"
    
    echo "[F3] Latency injection active. Waiting ${DURATION}s..."
    sleep "$DURATION"
    
    echo "[F3] Removing latency injection..."
    docker exec "$CONTAINER" sh -c "tc qdisc del dev eth0 root netem" 2>/dev/null || true
    echo "[F3] Database latency injection removed."
    ;;

  rabbitmq_restart)
    # F4: Restart RabbitMQ broker during active message processing
    CONTAINER="${CONTAINER:-neptune-rabbitmq}"
    echo "[F4] Restarting RabbitMQ broker: $CONTAINER"
    
    CRASH_TIME=$(date +%s%N)
    docker restart "$CONTAINER"
    
    # Wait for RabbitMQ to be ready
    for i in $(seq 1 30); do
      if docker exec "$CONTAINER" rabbitmq-diagnostics check_port_connectivity 2>/dev/null; then
        HEALTHY_TIME=$(date +%s%N)
        MTTR=$(( (HEALTHY_TIME - CRASH_TIME) / 1000000 ))
        echo "[F4] RabbitMQ recovered. MTTR: ${MTTR}ms"
        break
      fi
      sleep 2
    done
    ;;

  consumer_crash)
    # F5: Kill Kafka consumers and measure rebalance time
    echo "[F5] Simulating consumer crash..."
    echo "[F5] Recording consumer group status before crash:"
    docker exec neptune-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
      --bootstrap-server localhost:9092 \
      --describe --group neptune-transaction-group 2>/dev/null || true
    
    CRASH_TIME=$(date +%s%N)
    
    # Kill and restart the transaction service (which hosts consumers)
    docker kill neptune-transaction-service
    echo "[F5] Transaction service (consumer) killed at $(date -Iseconds)"
    
    sleep 15  # Wait for rebalance to trigger
    
    docker start neptune-transaction-service
    echo "[F5] Transaction service restarted."
    
    # Monitor rebalance
    sleep 30
    echo "[F5] Consumer group status after recovery:"
    docker exec neptune-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
      --bootstrap-server localhost:9092 \
      --describe --group neptune-transaction-group 2>/dev/null || true
    
    RECOVERY_TIME=$(date +%s%N)
    REBALANCE_MS=$(( (RECOVERY_TIME - CRASH_TIME) / 1000000 ))
    echo "[F5] Total rebalance time: ${REBALANCE_MS}ms"
    ;;

  *)
    echo "ERROR: Unknown fault type: $FAULT_TYPE"
    echo "Available: kafka_crash, service_crash, db_slowdown, rabbitmq_restart, consumer_crash"
    exit 1
    ;;

esac

echo ""
echo "[DONE] Fault injection complete."

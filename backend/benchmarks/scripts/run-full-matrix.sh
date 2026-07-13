#!/bin/bash
# ==============================================================================
# Full Experiment Matrix Runner
# Project: Neptune Bank — IEEE Research
# Author: Ayshi Shannidhya Panda
#
# Runs the complete experimental matrix:
#   3 paradigms × 6 user levels × 5 runs = 90 experiments
#
# Usage:
#   ./run-full-matrix.sh
#
# WARNING: This will take many hours to complete.
# Estimated time: ~90 × 7 min ≈ 10.5 hours (minimum)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

PARADIGMS=("rest" "rabbitmq" "kafka")
USER_LEVELS=(100 500 1000 5000 10000 20000)
SCENARIO="fund_transfer"
NUM_RUNS=5
COOLDOWN=120  # seconds between experiments

TOTAL=$((${#PARADIGMS[@]} * ${#USER_LEVELS[@]} * NUM_RUNS))
CURRENT=0
START_TIME=$(date +%s)

echo "============================================================"
echo " NEPTUNE BANK — FULL EXPERIMENT MATRIX"
echo "============================================================"
echo " Paradigms:    ${PARADIGMS[*]}"
echo " User Levels:  ${USER_LEVELS[*]}"
echo " Runs per:     $NUM_RUNS"
echo " Total:        $TOTAL experiments"
echo " Scenario:     $SCENARIO"
echo " Estimated:    ~$((TOTAL * 7)) minutes"
echo "============================================================"
echo ""

# Log file
LOG_FILE="$SCRIPT_DIR/../results/experiment_matrix_$(date +%Y%m%d_%H%M%S).log"
mkdir -p "$(dirname "$LOG_FILE")"

for paradigm in "${PARADIGMS[@]}"; do
    for users in "${USER_LEVELS[@]}"; do
        for run in $(seq 1 $NUM_RUNS); do
            CURRENT=$((CURRENT + 1))
            ELAPSED=$(($(date +%s) - START_TIME))
            
            echo ""
            echo "============================================================"
            echo " EXPERIMENT $CURRENT / $TOTAL"
            echo " Paradigm: $paradigm | Users: $users | Run: $run"
            echo " Elapsed: $((ELAPSED / 60)) min | $(date)"
            echo "============================================================"
            
            # Run the experiment
            if "$SCRIPT_DIR/run-experiment.sh" "$paradigm" "$SCENARIO" "$users" "$run" 2>&1 | tee -a "$LOG_FILE"; then
                echo "[MATRIX] Experiment $CURRENT completed successfully" | tee -a "$LOG_FILE"
            else
                echo "[MATRIX] WARNING: Experiment $CURRENT FAILED" | tee -a "$LOG_FILE"
            fi
            
            # Cooldown between experiments
            if [ $CURRENT -lt $TOTAL ]; then
                echo "[MATRIX] Cooldown: ${COOLDOWN}s before next experiment..."
                sleep $COOLDOWN
            fi
        done
    done
done

TOTAL_TIME=$(($(date +%s) - START_TIME))

echo ""
echo "============================================================"
echo " EXPERIMENT MATRIX COMPLETE"
echo "============================================================"
echo " Total experiments: $TOTAL"
echo " Total time:        $((TOTAL_TIME / 3600))h $((TOTAL_TIME % 3600 / 60))m"
echo " Log file:          $LOG_FILE"
echo ""
echo " Next step:"
echo "   python3 $SCRIPT_DIR/analyze_results.py \\"
echo "     --input-dir $SCRIPT_DIR/../results/ \\"
echo "     --output-dir $SCRIPT_DIR/../analysis/"
echo "============================================================"

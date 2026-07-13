<#
.SYNOPSIS
    Neptune Bank — IEEE Research Experiment Runner (Windows PowerShell)
.DESCRIPTION
    Runs a single benchmark experiment for the IEEE research paper.
    This is the Windows-native equivalent of run-experiment.sh.
.PARAMETER Paradigm
    Communication paradigm: rest, rabbitmq, or kafka
.PARAMETER Users
    Number of concurrent JMeter threads
.PARAMETER Duration
    Test duration in seconds (default: 300)
.PARAMETER RunNumber
    Run number for this experiment (1-5)
.EXAMPLE
    .\Run-Experiment.ps1 -Paradigm kafka -Users 1000 -RunNumber 1
    .\Run-Experiment.ps1 -Paradigm rest -Users 100 -Duration 60 -RunNumber 1
#>

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("rest","rabbitmq","kafka")]
    [string]$Paradigm,

    [Parameter(Mandatory=$true)]
    [int]$Users,

    [int]$Duration = 300,
    [int]$RampUp = 60,
    [int]$RunNumber = 1,
    [int]$WarmupDuration = 60,
    [string]$Scenario = "fund_transfer"
)

$ErrorActionPreference = "Stop"
$ProjectDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$ResultsDir = Join-Path $ProjectDir "benchmarks\results"
$JMeterDir  = Join-Path $ProjectDir "benchmarks\jmeter"
$Timestamp  = Get-Date -Format "yyyyMMdd_HHmmss"
$ResultFile = "${Paradigm}_${Scenario}_${Users}_run${RunNumber}"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Neptune Bank — IEEE Experiment Runner"    -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Paradigm:    $Paradigm"
Write-Host " Scenario:    $Scenario"
Write-Host " Users:       $Users"
Write-Host " Duration:    ${Duration}s"
Write-Host " Run:         $RunNumber"
Write-Host " Timestamp:   $Timestamp"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Create results directory
$OutputDir = Join-Path $ResultsDir $Timestamp
New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

# ===========================
# STEP 1: Start Docker stack
# ===========================
Write-Host "[STEP 1/6] Starting Docker Compose stack ($Paradigm mode)..." -ForegroundColor Yellow
Set-Location $ProjectDir
docker compose -f docker-compose.yml -f "docker-compose.${Paradigm}.yml" up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker Compose failed to start!" -ForegroundColor Red
    exit 1
}

# ===========================
# STEP 2: Wait for services
# ===========================
Write-Host ""
Write-Host "[STEP 2/6] Waiting for services to become healthy..." -ForegroundColor Yellow

$services = @(
    @{ Name = "Account Service";     Url = "http://localhost:8083/actuator/health" },
    @{ Name = "Transaction Service"; Url = "http://localhost:8084/actuator/health" }
)

foreach ($svc in $services) {
    $retries = 30
    while ($retries -gt 0) {
        try {
            $response = Invoke-WebRequest -Uri $svc.Url -TimeoutSec 3 -UseBasicParsing -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "[HEALTH] $($svc.Name) is UP" -ForegroundColor Green
                break
            }
        } catch { }
        $retries--
        Write-Host "[HEALTH] $($svc.Name) not ready. Retrying in 10s... ($retries left)"
        Start-Sleep -Seconds 10
    }
    if ($retries -eq 0) {
        Write-Host "[ERROR] $($svc.Name) failed to start!" -ForegroundColor Red
        docker compose -f docker-compose.yml -f "docker-compose.${Paradigm}.yml" down --remove-orphans
        exit 1
    }
}

# ===========================
# STEP 3: Warm-up
# ===========================
Write-Host ""
Write-Host "[STEP 3/6] Running warm-up phase (${WarmupDuration}s at 50% load)..." -ForegroundColor Yellow
$WarmupUsers = [Math]::Max(10, [Math]::Floor($Users / 2))

$jmxPath = Join-Path $JMeterDir "${Scenario}.jmx"
$warmupResultPath = Join-Path $OutputDir "warmup_${ResultFile}.csv"

jmeter -n `
    -t $jmxPath `
    -Jusers=$WarmupUsers `
    -Jduration=$WarmupDuration `
    -Jrampup=30 `
    -l $warmupResultPath 2>&1 | Select-Object -Last 5

Write-Host "[WARMUP] Complete. Pausing 10s before measurement..." -ForegroundColor Green
Start-Sleep -Seconds 10

# ===========================
# STEP 4: Run load test
# ===========================
Write-Host ""
Write-Host "[STEP 4/6] Running load test: $Users users for ${Duration}s..." -ForegroundColor Yellow

$testResultPath  = Join-Path $OutputDir "${ResultFile}.csv"
$reportPath      = Join-Path $OutputDir "${ResultFile}_report"

jmeter -n `
    -t $jmxPath `
    -Jusers=$Users `
    -Jduration=$Duration `
    -Jrampup=$RampUp `
    -l $testResultPath `
    -e -o $reportPath 2>&1 | Select-Object -Last 20

Write-Host "[JMETER] Load test complete." -ForegroundColor Green

# ===========================
# STEP 5: Collect Prometheus metrics
# ===========================
Write-Host ""
Write-Host "[STEP 5/6] Collecting Prometheus metrics..." -ForegroundColor Yellow

$MetricsDir = Join-Path $OutputDir "metrics"
New-Item -ItemType Directory -Path $MetricsDir -Force | Out-Null

$metrics = @(
    "neptune_communication_latency_seconds_count",
    "neptune_communication_latency_seconds_sum",
    "neptune_communication_errors_total",
    "process_cpu_usage",
    "jvm_memory_used_bytes"
)

foreach ($metric in $metrics) {
    try {
        $result = Invoke-WebRequest -Uri "http://localhost:9090/api/v1/query?query=$metric" -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
        $result.Content | Out-File (Join-Path $MetricsDir "$metric.json") -Encoding UTF8
    } catch {
        Write-Host "[METRICS] WARNING: Failed to collect $metric"
    }
}

# ===========================
# STEP 6: Docker stats snapshot
# ===========================
Write-Host ""
Write-Host "[STEP 6/6] Recording container resource snapshot..." -ForegroundColor Yellow
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" | Out-File (Join-Path $OutputDir "${ResultFile}_docker_stats.txt") -Encoding UTF8

# ===========================
# CLEANUP
# ===========================
Write-Host ""
Write-Host "[CLEANUP] Stopping Docker Compose stack..." -ForegroundColor Yellow
docker compose -f docker-compose.yml -f "docker-compose.${Paradigm}.yml" down --remove-orphans

# ===========================
# SUMMARY
# ===========================
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host " EXPERIMENT COMPLETE" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host " Results:     $OutputDir"
Write-Host " JMeter CSV:  ${ResultFile}.csv"
Write-Host " JMeter HTML: ${ResultFile}_report\"
Write-Host " Metrics:     metrics\"
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host " Next: .\Run-Experiment.ps1 -Paradigm $Paradigm -Users $Users -RunNumber $($RunNumber + 1)"

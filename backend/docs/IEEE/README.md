# Neptune Bank — IEEE Research: Performance Evaluation of Event-Driven Banking Microservices

## Paper Title
**Performance Evaluation of Event-Driven Banking Microservices Using Apache Kafka and Spring Boot Under High-Concurrency Workloads**

**Author:** Ayshi Shannidhya Panda  
**Institution:** Silicon Institute of Technology, Sambalpur, Odisha, India  
**Email:** asp45624@gmail.com

---

## Repository Structure

```
Backend/
├── docs/IEEE/
│   ├── paper/
│   │   ├── main.tex              # IEEE manuscript (IEEEtran class)
│   │   └── references.bib        # Verified BibTeX references
│   └── literature-review/
│       └── annotated_bibliography.md
│
├── account-service/              # Account management (Kafka + RabbitMQ + REST)
├── auth-service/                 # JWT authentication
├── transaction-service/          # Fund transfers (3 communication strategies)
├── user-service/                 # User profiles + KYC
├── otp-service/                  # OTP verification
├── notification-service/         # [NEW] Event-driven notifications
├── audit-service/                # [NEW] Transaction audit trail
│
├── monitoring/
│   ├── prometheus/prometheus.yml # Prometheus scrape config
│   └── grafana/provisioning/     # Grafana auto-provisioning
│
├── benchmarks/
│   ├── jmeter/                   # JMeter test plans
│   ├── chaos/                    # Chaos engineering scripts
│   └── scripts/
│       ├── run-experiment.sh     # Single experiment runner
│       ├── run-full-matrix.sh    # Full 90-experiment matrix
│       └── analyze_results.py    # Statistical analysis
│
├── docker-compose.yml            # Base orchestration
├── docker-compose.kafka.yml      # Kafka experiment overlay
├── docker-compose.rabbitmq.yml   # RabbitMQ experiment overlay
└── docker-compose.rest.yml       # REST experiment overlay
```

---

## Quick Start

### Prerequisites
- Docker & Docker Compose v2
- Java 21 (Temurin)
- Apache JMeter 5.6+
- Python 3.10+ (for analysis: `pip install pandas numpy scipy matplotlib seaborn`)

### Running an Experiment

```bash
# 1. Start with Kafka communication
docker-compose -f docker-compose.yml -f docker-compose.kafka.yml up -d --build

# 2. Wait for services to be healthy
curl http://localhost:8084/actuator/health

# 3. Run JMeter load test (1000 users, 5 min)
jmeter -n -t benchmarks/jmeter/fund_transfer.jmx \
  -Jusers=1000 -Jduration=300 -Jrampup=60 \
  -l results/kafka_fund_transfer_1000_run1.csv

# 4. Repeat with RabbitMQ
docker-compose down
docker-compose -f docker-compose.yml -f docker-compose.rabbitmq.yml up -d --build
# ... run JMeter again ...

# 5. Repeat with REST
docker-compose down
docker-compose -f docker-compose.yml -f docker-compose.rest.yml up -d --build
# ... run JMeter again ...

# 6. Analyze results
python3 benchmarks/scripts/analyze_results.py \
  --input-dir results/ --output-dir analysis/
```

### Full Experiment Matrix
```bash
# Runs 90 experiments (3 paradigms × 6 user levels × 5 runs)
# WARNING: Takes ~10 hours
./benchmarks/scripts/run-full-matrix.sh
```

---

## Communication Strategy Pattern

The key architectural contribution is the `CommunicationStrategy` interface that enables switching between REST, RabbitMQ, and Kafka via Spring Profiles:

```
--spring.profiles.active=rest      # Synchronous HTTP
--spring.profiles.active=rabbitmq  # RabbitMQ RPC
--spring.profiles.active=kafka     # Kafka event streaming
```

No business logic code changes between experiments.

---

## Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/neptune)
- **Kafka Exporter**: http://localhost:9308/metrics
- **cAdvisor**: http://localhost:8089

---

## License
Copyright © 2025–2026 Ayshi Shannidhya Panda. All rights reserved.

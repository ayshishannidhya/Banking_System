# 🏦 Neptune Bank — Online Banking System

A secure, scalable, and microservices-based online banking platform built using **Spring Boot 3.5**, **Java 21**, **PostgreSQL**, **RabbitMQ**, and **Apache Kafka**.

Neptune Bank provides a complete digital banking solution with user onboarding, KYC verification, account management, secure fund transfers, transaction tracking, OTP-based verification, and JWT-based authentication.

> **📄 IEEE Research**: This project also serves as the experimental testbed for an IEEE journal paper evaluating the performance of event-driven banking microservices. See [Research Section](#-ieee-research-paper) below.

---

## 🚀 Features

### 👤 User Management
- User Registration with KYC verification
- Profile Management (CRUD Operations)
- Soft Delete Support
- Contact & Nominee Management
- Employee-based KYC Verification

### 🏦 Account Management
- Savings Account
- Current Account
- Fixed Deposit Account
- Branch Association
- Balance Management
- Optimistic Locking Support

### 💸 Fund Transfers
- Account-to-Account Transfers
- Multi-paradigm Processing (REST / RabbitMQ / Kafka)
- Automatic Rollback on Failures
- Transaction Tracking

### 📜 Transaction History
- Complete Audit Trail
- Account-wise History
- Chronological Ordering
- Status Tracking

### 🔐 Security
- JWT Authentication (RS256)
- RSA Key Pair Signing
- BCrypt Password Hashing
- Spring Security Integration
- Role-Based Access Control (RBAC)

### 📱 OTP Verification
- SMS OTP via TextBee API
- Email OTP via SMTP
- OTP Expiry Management
- Verification Tracking

---

# 🛠 Technology Stack

| Category | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Build Tool | Gradle |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Messaging | RabbitMQ, Apache Kafka (KRaft) |
| Authentication | JWT (RS256) |
| Security | Spring Security |
| Password Hashing | BCrypt |
| Mail Service | Spring Mail |
| SMS Gateway | TextBee API |
| Containerization | Docker, Docker Compose |
| Monitoring | Prometheus, Grafana, cAdvisor |
| Benchmarking | Apache JMeter 5.6 |

---

# 🏗 Microservices Architecture

The system consists of **seven** independent microservices.

| Service | Port | Responsibility |
|----------|------|---------------|
| Auth Service | 8086 | Authentication & JWT |
| User Service | 8080 | User Profiles & KYC |
| OTP Service | 8082 | OTP Generation & Verification |
| Account Service | 8083 | Account Management |
| Transaction Service | 8084 | Fund Transfers & Transactions |
| Notification Service | 8087 | Event-driven Transaction Notifications |
| Audit Service | 8088 | Transaction Audit Trail |

---

## System Architecture

```text
                       ┌─────────────────┐
                       │     Client      │
                       └────────┬────────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │     Auth Service    │
                    │      Port 8086      │
                    └────────┬────────────┘
                             │ JWT
                             ▼

 ┌─────────────────┐  REST/RabbitMQ/Kafka  ┌─────────────────┐
 │  User Service   │◄────────────────────►│ Account Service │
 │    Port 8080    │                      │    Port 8083    │
 └─────────────────┘                      └────────┬────────┘
                                                    │
                                                    ▼
                                           ┌─────────────────┐
                                           │Transaction Svc │
                                           │    Port 8084   │
                                           └───┬────────┬───┘
                                               │        │
                              ┌────────────────┘        └────────────────┐
                              ▼                                          ▼
                    ┌─────────────────┐                        ┌─────────────────┐
                    │   OTP Service   │                        │Notification Svc │
                    │    Port 8082    │                        │    Port 8087    │
                    └─────────────────┘                        └─────────────────┘
                                                                        │
                                                               ┌───────┘
                                                               ▼
                                                      ┌─────────────────┐
                                                      │  Audit Service  │
                                                      │    Port 8088    │
                                                      └─────────────────┘
```

---

## 🔄 Communication Strategy Pattern

The core architectural feature is a **profile-switchable communication pattern** that enables the same business logic to run over three different paradigms:

```
--spring.profiles.active=rest       # Synchronous HTTP
--spring.profiles.active=rabbitmq   # RabbitMQ RPC
--spring.profiles.active=kafka      # Kafka event streaming
```

```text
TransactionService
       │
       ├── @Profile("rest")     → RestCommunicationStrategy     → HTTP POST → AccountRestController
       ├── @Profile("rabbitmq") → AccountValidationClient       → RabbitMQ  → AccountValidationListener
       └── @Profile("kafka")    → KafkaCommunicationStrategy    → Kafka     → KafkaAccountEventListener
                                                                                    │
                                                                           AccountService (shared)
                                                                                    │
                                                                             AccountRepository
                                                                                    │
                                                                              PostgreSQL DB
```

---

## 🔄 RabbitMQ Communication

### Queues

| Queue Name | Producer | Consumer |
|-------------|----------|----------|
| user-validation-queue | account-service | user-service |
| account-validation-queue | transaction-service | account-service |
| account-debit-queue | transaction-service | account-service |
| account-credit-queue | transaction-service | account-service |

---

## 📨 Kafka Topics

| Topic | Producer | Consumer |
|-------|----------|----------|
| account.validation | transaction-service | account-service |
| account.debit | transaction-service | account-service |
| account.credit | transaction-service | account-service |
| transaction.events | transaction-service | notification-service, audit-service |
| notification.events | notification-service | — |
| audit.events | audit-service | — |

---

# 🔑 Authentication Flow

```text
Client Login
     │
     ▼
Auth Service
     │
     ▼
Validate Credentials
     │
     ▼
Generate JWT Token
     │
     ▼
Return JWT
     │
     ▼
Use JWT for API Requests
```

## JWT Payload

```json
{
  "sub": "john_doe",
  "role": "USER",
  "iat": 1718150400,
  "exp": 1718161200
}
```

---

# 👥 User Roles

| Role | Access |
|--------|--------|
| ADMIN | Full System Access |
| EMPLOYEE | KYC Verification & Customer Management |
| USER | Banking Operations |

---

# 📂 Project Structure

```
Backend/
├── account-service/          # Account CRUD, balance, Kafka/RabbitMQ/REST listeners
├── auth-service/             # JWT authentication, login
├── transaction-service/      # Fund transfers, communication strategy pattern
├── user-service/             # User profiles, KYC
├── otp-service/              # OTP generation & verification
├── notification-service/     # Kafka event-driven notifications
├── audit-service/            # Kafka transaction audit trail
│
├── docker-compose.yml        # Base orchestration (all services + DBs + monitoring)
├── docker-compose.kafka.yml  # Kafka experiment overlay
├── docker-compose.rabbitmq.yml  # RabbitMQ experiment overlay
├── docker-compose.rest.yml   # REST experiment overlay
│
├── monitoring/
│   ├── prometheus/prometheus.yml
│   └── grafana/provisioning/
│
├── benchmarks/
│   ├── jmeter/fund_transfer.jmx
│   ├── chaos/fault-injection.sh
│   └── scripts/
│       ├── Run-Experiment.ps1
│       ├── run-experiment.sh
│       ├── run-full-matrix.sh
│       └── analyze_results.py
│
└── docs/IEEE/
    ├── paper/main.tex         # IEEE manuscript
    ├── paper/references.bib   # 25 verified citations
    └── literature-review/     # Annotated bibliography
```

---

# 🐳 Docker Quick Start

```bash
# Start with RabbitMQ (default mode)
docker compose -f docker-compose.yml -f docker-compose.rabbitmq.yml up -d --build

# Start with Kafka
docker compose -f docker-compose.yml -f docker-compose.kafka.yml up -d --build

# Start with REST (synchronous)
docker compose -f docker-compose.yml -f docker-compose.rest.yml up -d --build
```

### Monitoring

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / neptune |
| RabbitMQ Management | http://localhost:15672 | neptune / ankit |
| cAdvisor | http://localhost:8089 | — |

---

# 📄 IEEE Research Paper

**Title**: *Performance Evaluation of Event-Driven Banking Microservices Using Apache Kafka and Spring Boot Under High-Concurrency Workloads*

**Author**: Ayshi Shannidhya Panda
**Institution**: Silicon Institute of Technology, Sambalpur, Odisha, India

### Research Questions

| ID | Research Question |
|----|------------------|
| RQ1 | How does throughput scale across REST, RabbitMQ, and Kafka under increasing concurrency? |
| RQ2 | What are the latency distributions (p50, p95, p99) for each paradigm? |
| RQ3 | How does each paradigm utilize CPU and memory resources? |
| RQ4 | How does each paradigm respond to infrastructure faults? |

### Experiment Matrix

- **3 paradigms** × **6 user levels** (100, 500, 1K, 5K, 10K, 20K) × **5 runs** = **90 experiments**
- Statistical analysis: ANOVA, Shapiro-Wilk, Kruskal-Wallis, Cohen's d, 95% CI

### Running Experiments

```powershell
# Single experiment (Windows)
.\Backend\benchmarks\scripts\Run-Experiment.ps1 -Paradigm kafka -Users 1000 -RunNumber 1

# Full matrix (Bash / WSL)
./Backend/benchmarks/scripts/run-full-matrix.sh

# Analyze results
python Backend/benchmarks/scripts/analyze_results.py --input-dir results/ --output-dir analysis/
```

### Compile Paper

```bash
cd Backend/docs/IEEE/paper
pdflatex main.tex && bibtex main && pdflatex main.tex && pdflatex main.tex
```

---

# ✅ Highlights

✅ Microservice Architecture (7 services)

✅ Multi-paradigm Communication (REST / RabbitMQ / Kafka)

✅ Strategy Pattern for Runtime Paradigm Switching

✅ JWT Authentication (RS256)

✅ BCrypt Security

✅ OTP Verification (SMS + Email)

✅ KYC Document Management

✅ Saga-Based Transaction Rollback

✅ Role-Based Access Control

✅ Docker Compose with Monitoring (Prometheus + Grafana)

✅ IEEE Research Paper with LaTeX Manuscript

✅ Automated Benchmarking Suite (JMeter + Statistical Analysis)

✅ Chaos Engineering (5 Fault Injection Scenarios)

✅ Scalable Architecture

---

# 👨‍💻 Author

**Ayshi Shannidhya Panda**

B.Tech. CSE, Silicon Institute of Technology, Sambalpur, Odisha, India

GitHub:
https://github.com/ayshishannidhya

LinkedIn:
https://www.linkedin.com/in/ayshishannidhya/

Email:
asp45624@gmail.com

---

# 📜 License

Copyright © 2025-2026 Ayshi Shannidhya Panda.

This project is distributed under a Proprietary License.
Unauthorized copying, modification, distribution, or commercial use is prohibited without explicit permission from the author.

---

# ⭐ Neptune Bank

A modern microservices-based banking platform designed for security, scalability, reliability, and enterprise-grade banking operations — with IEEE-quality research infrastructure for empirical performance evaluation.

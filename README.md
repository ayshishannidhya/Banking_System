# 🏦 Neptune Bank - Online Banking System

A secure, scalable, and microservices-based online banking platform built using **Spring Boot 3.5**, **Java 21**, **PostgreSQL**, and **RabbitMQ**.

Neptune Bank provides a complete digital banking solution with user onboarding, KYC verification, account management, secure fund transfers, transaction tracking, OTP-based verification, and JWT-based authentication.

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
- RabbitMQ-based Processing
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
| Messaging | RabbitMQ |
| Authentication | JWT (RS256) |
| Security | Spring Security |
| Password Hashing | BCrypt |
| Mail Service | Spring Mail |
| SMS Gateway | TextBee API |

---

# 🏗 Microservices Architecture

The system consists of five independent microservices.

| Service | Port | Responsibility |
|----------|------|---------------|
| Auth Service | 8086 | Authentication & JWT |
| User Service | 8080 | User Profiles & KYC |
| OTP Service | 8082 | OTP Generation & Verification |
| Account Service | 8083 | Account Management |
| Transaction Service | 8084 | Fund Transfers & Transactions |

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

 ┌─────────────────┐    RabbitMQ    ┌─────────────────┐
 │  User Service   │◄──────────────►│ Account Service │
 │    Port 8080    │                │    Port 8083    │
 └─────────────────┘                └────────┬────────┘
                                              │
                                              ▼
                                     ┌─────────────────┐
                                     │Transaction Svc │
                                     │    Port 8084   │
                                     └────────┬───────┘
                                              │
                                              ▼
                                     ┌─────────────────┐
                                     │   OTP Service   │
                                     │    Port 8082    │
                                     └─────────────────┘
```

---

# 🔄 RabbitMQ Communication

## Queues

| Queue Name | Producer | Consumer |
|-------------|----------|----------|
| user-validation-queue | account-service | user-service |
| account-validation-queue | transaction-service | account-service |
| account-debit-queue | transaction-service | account-service |
| account-credit-queue | transaction-service | account-service |

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

# 📂 Services

---

## 🔐 Auth Service (Port 8086)

### Responsibilities

- Login Authentication
- JWT Generation
- User Credential Management
- Role Assignment

### Endpoints

```http
POST /auth/login
POST /auth/add-user
GET  /auth/validate
```

### Login Request

```json
{
  "username": "john_doe",
  "password": "password123",
  "mode": "jwt"
}
```

---

## 👤 User Service (Port 8080)

### Responsibilities

- User Registration
- Profile Management
- KYC Document Management
- Nominee Management

### KYC Documents Supported

- Aadhaar
- PAN
- Passport
- Voter ID
- Driving License
- User Photograph
- Signature

### Endpoints

```http
POST   /auth/user
GET    /auth/user/{id}
GET    /auth/users
PUT    /auth/user/{id}
DELETE /auth/user/{id}
GET    /auth/user/exists/{id}
```

---

## 🏦 Account Service (Port 8083)

### Responsibilities

- Account Creation
- Balance Management
- Branch Management
- Account Validation

### Supported Account Types

```java
SAVINGS
CURRENT
FIXED_DEPOSIT
```

### Endpoints

```http
POST   /api/accounts/create
GET    /api/accounts/get/{id}
GET    /api/accounts/get/all
GET    /api/accounts/user/{userId}
GET    /api/accounts/number/{number}
PUT    /api/accounts/update/{id}
DELETE /api/accounts/delete/{id}
```

---

## 💸 Transaction Service (Port 8084)

### Responsibilities

- Fund Transfers
- Transaction Tracking
- Audit Trail

### Endpoints

```http
POST /transactions/create
POST /transactions/transfer
GET  /transactions/{transactionId}
GET  /transactions/account/{accountId}
GET  /transactions/all
```

### Fund Transfer Workflow

```text
Validate Accounts
       │
       ▼
Create Transaction (PENDING)
       │
       ▼
Debit Source Account
       │
       ▼
Credit Destination Account
       │
       ▼
Success
```

### Rollback Strategy

```text
Debit Success
      │
      ▼
Credit Failed
      │
      ▼
Refund Source Account
      │
      ▼
Mark Transaction Failed
```

---

## 📱 OTP Service (Port 8082)

### Responsibilities

- OTP Generation
- OTP Verification
- SMS Delivery
- Email Delivery

### Endpoints

```http
POST /api/otp/send
POST /api/otp/verify
```

### OTP Properties

| Property | Value |
|-----------|--------|
| Length | 6 Digits |
| Generator | SecureRandom |
| Expiry | 5 Minutes |
| Usage | One Time |

---

# 🗄 Database Setup

Create the following databases:

```sql
CREATE DATABASE AuthDB;
CREATE DATABASE UserDB;
CREATE DATABASE OtpDB;
CREATE DATABASE AccountDB;
CREATE DATABASE TransactionDB;
```

### Database Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433
spring.datasource.username=neptune
spring.datasource.password=********
```

---

# ⚙ Prerequisites

- Java 21
- PostgreSQL 16
- RabbitMQ
- Gradle
- RSA Public Key
- RSA Private Key

---

# 🔨 Build Project

```bash
cd auth-service
./gradlew build -x test

cd ../user-service
./gradlew build -x test

cd ../otp-service
./gradlew build -x test

cd ../account-service
./gradlew build -x test

cd ../transaction-service
./gradlew build -x test
```

---

# ▶ Running Services

Start services in the following order:

```text
1. Auth Service
2. OTP Service
3. User Service
4. Account Service
5. Transaction Service
```

---

# 📚 API Quick Reference

## Authentication

```http
POST /auth/login
POST /auth/add-user
GET  /auth/validate
```

## User APIs

```http
POST   /auth/user
GET    /auth/user/{id}
GET    /auth/users
PUT    /auth/user/{id}
DELETE /auth/user/{id}
```

## Account APIs

```http
POST   /api/accounts/create
GET    /api/accounts/get/{id}
GET    /api/accounts/get/all
PUT    /api/accounts/update/{id}
DELETE /api/accounts/delete/{id}
```

## Transaction APIs

```http
POST /transactions/create
POST /transactions/transfer
GET  /transactions/{id}
GET  /transactions/account/{accountId}
```

## OTP APIs

```http
POST /api/otp/send
POST /api/otp/verify
```

---

# ⚠ Error Response Format

```json
{
  "success": false,
  "message": "Resource not found",
  "errors": [
    "validation message"
  ]
}
```

---

# 📈 Highlights

✅ Spring Boot Microservices

✅ PostgreSQL Persistence

✅ RabbitMQ Messaging

✅ JWT Authentication

✅ BCrypt Security

✅ OTP Verification

✅ KYC Document Management

✅ Saga-Based Transaction Rollback

✅ Role-Based Access Control

✅ Scalable Architecture

---

# 👨‍💻 Author

**Ayshi Shannidhya Panda**

GitHub:
https://github.com/ayshishannidhya

Email:
a.shannidhya@gmail.com

---

# 📜 License

Copyright © 2025-2026 Ayshi Shannidhya Panda.

This project is distributed under a Proprietary License.
Unauthorized copying, modification, distribution, or commercial use is prohibited without explicit permission from the author.

---

# ⭐ Neptune Bank

A modern microservices-based banking platform designed for security, scalability, reliability, and enterprise-grade banking operations.

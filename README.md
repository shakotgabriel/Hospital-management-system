<div align="center">

<br/>

```
██╗  ██╗███╗   ███╗███████╗
██║  ██║████╗ ████║██╔════╝
███████║██╔████╔██║███████╗
██╔══██║██║╚██╔╝██║╚════██║
██║  ██║██║ ╚═╝ ██║███████║
╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝
```

**Hospital Management System**

*A production-grade API for real hospital operations — secure, extensible, and contributor-ready.*

<br/>

[![Java](https://img.shields.io/badge/Java-17+-F89820?style=for-the-badge\&logo=openjdk\&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge\&logo=springboot\&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-336791?style=for-the-badge\&logo=postgresql\&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge\&logo=jsonwebtokens\&logoColor=white)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge\&logo=apachemaven\&logoColor=white)](https://maven.apache.org/)

[![Status](https://img.shields.io/badge/Status-Active-22c55e?style=flat-square)](.)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-8b5cf6?style=flat-square)](CONTRIBUTING.md)
[![License](https://img.shields.io/badge/License-MIT-f59e0b?style=flat-square)](.)

<br/>

[**Get Started**](#-quick-start) · [**API Docs**](#-api-testing-postman) · [**Contribute**](#-contributing) · [**Roadmap**](#-roadmap)

<br/>

</div>

---

## What is this?

HMS is a **security-first REST API** for hospital operations — built with Spring Boot and designed to reflect real healthcare team workflows.

It ships with:

* ✅ JWT-based auth with explicit role-gated routes
* ✅ Full staff lifecycle: admin, receptionist, doctor, cashier
* ✅ Patient records, appointments, billing, and prescription workflows
* ✅ Consistent response envelope + centralized exception handling
* ✅ Clean layered architecture — readable, maintainable, and easy to extend

> **Security by default.** Public signup is disabled. Admin accounts are bootstrapped from environment variables. Every route guard is declared explicitly in `SecurityConfig.java`.

### 🌍 Real-World Context

This system is designed with healthcare environments like those in developing regions in mind, where:

* Strict role separation is critical for accountability
* Patient data privacy is mandatory
* Paper-based workflows are still common

HMS can power:

* Private clinics
* Multi-branch hospitals
* Health-tech startups

### 🧩 Engineering Challenges Solved

* Explicit role-based authorization without over-reliance on annotations
* Prevented unauthorized system access via disabled public signup
* Consistent API response structure for frontend predictability
* Ownership checks for sensitive operations (e.g., prescriptions)
* Layered architecture for scalability and maintainability

---

## 🗺️ Architecture

```
Client / Postman
      │
      ▼
┌─────────────────────┐
│   Spring Boot API   │
│                     │
│  SecurityConfig     │  ← JWT validation + role guards
│  + JWT Filter       │
└────────┬────────────┘
         │
    ┌────▼─────┐
    │Controllers│  ← Route definitions & response contracts
    └────┬─────┘
         │
    ┌────▼─────┐
    │ Services  │  ← Business rules & ownership checks
    └────┬─────┘
         │
    ┌────▼──────────┐
    │ Repositories  │  ← Spring Data JPA
    └────┬──────────┘
         │
    ┌────▼──────────┐
    │  PostgreSQL   │  ← Source of truth
    └───────────────┘
```

### DevOps & Deployment (Planned)

* Dockerized backend service
* Jenkins CI/CD pipeline: build, test, security scan
* Future deployment: AWS / DigitalOcean, Nginx reverse proxy

---

## 🔐 Access Control Model

| Role           | Responsibilities                                             |
| -------------- | ------------------------------------------------------------ |
| `ADMIN`        | Full system access — users, roles, all operational endpoints |
| `RECEPTIONIST` | Patient intake, scheduling, front-desk billing               |
| `DOCTOR`       | Own profile, assigned appointments, prescriptions            |
| `CASHIER`      | Billing lifecycle — issue, partial, paid, void               |
| `PATIENT`      | Restricted by system policy                                  |

All route guards live in `backend/src/main/java/com/hospital/backend/config/SecurityConfig.java`.

---

## ⚡ Quick Start

### 1. Start PostgreSQL

```bash
cd backend
docker compose up -d
```

### 2. Configure Environment Variables

```bash
# Database
export DB_URL='jdbc:postgresql://localhost:5432/<your_db_name>'
export DB_USERNAME='<your_db_username>'
export DB_PASSWORD='<your_db_password>'

# Auth
export JWT_SECRET='<at_least_32_chars_strong_secret>'

# Admin seed account
export ADMIN_EMAIL='<your_admin_email>'
export ADMIN_PASSWORD='<your_admin_password>'
export ADMIN_FIRST_NAME='<first_name>'
export ADMIN_LAST_NAME='<last_name>'
```

Generate a strong JWT secret:

```bash
openssl rand -base64 48
```

> **Tip:** Add exports to `.zshrc`/`.bashrc` or use a local `.env` loader. Never commit secrets.

### 3. Run the API

```bash
cd backend
./mvnw spring-boot:run
```

### 4. Verify It's Working

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hospital.local","password":"your_password"}'
```

You should get `200 OK` with a JWT token. ✅

---



**Recommended Test Order:**

```
1. Auth smoke test          →  POST /api/auth/login (valid + invalid)
2. Admin user management    →  Create receptionist / cashier / doctor accounts
3. Reception workflows      →  Patients, appointments, billing
4. Doctor workflows         →  Confirm/complete appointments, prescriptions
5. Role boundary checks     →  Expect 403 on disallowed routes
```

**Assertions to Include:**

* `2xx` success, `400/401/403` failures
* Response envelope: `{ success, message, data, timestamp }`
* Required fields present
* Role field on login
* Baseline response time < 500ms

---

## 📂 Repository Structure

```
hospital-management-system/
├── backend/
│   ├── src/main/java/com/hospital/
│   │   ├── config/      # SecurityConfig, JWT filter
│   │   ├── controller/  # HTTP route handlers
│   │   ├── service/     # Business logic
│   │   ├── repository/  # JPA data access
│   │   └── entity/      # Domain models
│   └── docker-compose.yml

```

---

## 🤝 Contributing

All skill levels welcome.

**How to Contribute:**

```bash
# 1. Fork & clone
git clone https://github.com/your-username/hospital-management-system.git

# 2. Create feature branch
git checkout -b feat/your-feature-name

# 3. Validate locally
cd backend
./mvnw -q -DskipTests compile
./mvnw -q test

# 4. Push & open PR
```

**PR Guidelines:**

* One feature/fix per PR
* Preserve API contract
* Document new endpoints and role guards
* No unrelated refactors

---

## 🟢 Good First Issues

| ID       | Task                                                     | Difficulty | Impact |
| -------- | -------------------------------------------------------- | ---------- | ------ |
| `GFI-01` | Add integration tests for role-based route guards        | Easy       | High   |
| `GFI-02` | Improve validation error messages and detail fields      | Easy       | Medium |
| `GFI-03` | Expand Postman tests: positive + negative role scenarios | Easy       | High   |
| `GFI-04` | Add seed scripts for demo data                           | Medium     | High   |
| `GFI-05` | Add OpenAPI/Swagger annotations with examples            | Medium     | Medium |
| `GFI-06` | Improve CI: lint, test coverage, security scan           | Medium     | High   |

---

## 🛣️ Roadmap

* Fine-grained permission policies
* Audit trail for critical data changes
* Multi-hospital tenancy
* Notification system for appointments & billing
* OpenAPI-driven docs & contract validation
* Seed data packs for demo & QA
* Performance profiling for high-traffic clinics
* Dashboard metrics & reporting

---

## 🛠️ Tech Stack

| Layer       | Technology            |
| ----------- | --------------------- |
| Language    | Java 17+              |
| Framework   | Spring Boot           |
| Security    | Spring Security + JWT |
| ORM         | Spring Data JPA       |
| Database    | PostgreSQL            |
| Build       | Maven                 |
| API Testing | Postman               |

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">
**Built by contributors. Improved by the community.**

If HMS helps you — or teaches you something — consider ⭐ the repo and share.

</div>

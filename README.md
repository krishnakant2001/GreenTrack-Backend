<div align="center">

# 🌿 GreenTrack — Carbon Tracker API

**A production-ready REST API for tracking personal carbon footprint, setting green goals, and getting actionable sustainability recommendations.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green?style=for-the-badge&logo=mongodb)](https://www.mongodb.com/atlas)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)

</div>

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [API Reference](#-api-reference)
- [Data Models](#-data-models)
- [Security](#-security)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Docker Deployment](#-docker-deployment)
- [Project Structure](#-project-structure)
- [Future Improvements](#-future-improvements)

---

## 🌍 About the Project

**GreenTrack** is a backend REST API built for individuals who want to track, understand, and reduce their personal carbon footprint. Users can log daily activities like travel, energy usage, and purchases — and the system automatically calculates the corresponding **CO₂ equivalent (CO₂e) emissions** based on region-specific emission factors maintained by admins.

The platform provides:
- A personal **dashboard** with period-based emission analytics
- **Goal tracking** to stay on target with carbon reduction plans
- **AI-style recommendations** generated from the user's emission patterns

This project is designed with **production-grade architecture** in mind — JWT + session management, OTP verification, Google OAuth2, Redis caching, and full Docker support.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🔐 **Dual Authentication** | Email/password login + Google OAuth2 |
| 📧 **OTP Email Verification** | 6-digit OTP via SendGrid before account activation |
| 🪙 **JWT + Refresh Tokens** | Short-lived access tokens (15 days) + long-lived refresh tokens (90 days) stored in HttpOnly cookies |
| 📊 **Carbon Dashboard** | Daily / Weekly / Monthly emission summaries with percentage change vs previous period |
| ⚡ **Automatic Emission Calculation** | CO₂e calculated on activity save using region-specific emission factors |
| 🎯 **Goal Management** | Set, update, and track carbon reduction goals per category and time period |
| 💡 **Smart Recommendations** | Personalized recommendations generated from user's top-emission categories |
| 🛡️ **Role-Based Access Control** | `USER` and `ADMIN` roles with method-level security |
| 📄 **Paginated Activity Feed** | Infinite-scroll-ready paginated activity listing |
| 🗄️ **Redis OTP Caching** | OTPs cached in Redis with configurable TTL and attempt limits |
| 🌏 **Region-Aware Calculations** | Emission factors vary by region (`IN`, `US`, `EU`, etc.) |
| 🐳 **Dockerized** | Multi-stage Docker build with Docker Compose for full-stack local dev |
| 🔁 **Soft Delete** | Users and activities are soft-deleted, preserving audit trails |

---

## 🛠️ Tech Stack

### Core
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Database | MongoDB Atlas |
| Cache | Redis 7 |
| Email | SendGrid |

### Spring Modules
- `spring-boot-starter-web` — REST API layer
- `spring-boot-starter-security` — Authentication & Authorization
- `spring-boot-starter-oauth2-client` — Google OAuth2 login
- `spring-boot-starter-data-mongodb` — MongoDB ODM
- `spring-boot-starter-data-redis` — Redis integration
- `spring-boot-starter-validation` — Request DTO validation
- `spring-boot-starter-actuator` — Health monitoring endpoint

### Libraries
- **JJWT 0.12.6** — JWT generation and validation
- **ModelMapper 3.2.4** — DTO ↔ Entity mapping
- **Lombok** — Boilerplate reduction
- **Jackson JSR310** — Java 8 date/time serialization

### DevOps
- **Docker** — Multi-stage image build (Maven build → Temurin JRE runtime)
- **Docker Compose** — Orchestrates app + Redis containers

---

## 🏗️ System Architecture

```
                          ┌────────────────────────────────────┐
                          │           Client (Frontend)        │
                          └──────────────────┬─────────────────┘
                                             │ HTTPS
                          ┌──────────────────▼─────────────────┐
                          │        GreenTrack REST API         │
                          │          (Spring Boot)             │
                          │                                    │
                          │  ┌──────────────────────────────┐  │
                          │  │  JwtAuthFilter (per request) │  │
                          │  └──────────────┬───────────────┘  │
                          │                 │                  │
                          │  ┌──────────────▼──────────────┐   │
                          │  │     Controllers / Services  │   │
                          │  └──────────────┬──────────────┘   │
                          └─────────────────┼──────────────────┘
                                            │
                   ┌────────────────────────┼──────────────────────────┐
                   │                        │                          │
        ┌──────────▼──────────┐  ┌──────────▼──────────┐  ┌────────────▼─────────┐
        │    MongoDB Atlas    │  │     Redis Cache     │  │   SendGrid (Email)   │
        │ (Users, Activities, │  │  (OTP storage,      │  │  (OTP delivery,      │
        │   Goals, Sessions,  │  │ verification flags) │  │   notifications)     │
        │   EmissionFactors,  │  └─────────────────────┘  └──────────────────────┘
        │   Recommendations)  │
        └─────────────────────┘
```

---

## 📡 API Reference

> Base URL: `http://localhost:8080`

### 🔐 Auth Legend

| Symbol | Meaning |
|--------|---------|
| 🔓 `No Token` | **Public endpoint** — No JWT required. Anyone can call it. |
| 🔒 `USER` | **Protected** — Requires a valid JWT with `ROLE_USER` or `ROLE_ADMIN` |
| 🔒 `ADMIN` | **Admin only** — Requires a valid JWT with `ROLE_ADMIN` |

> **Why do so many endpoints say `🔓 No Token`?**
>
> This is intentional and standard in every REST API. Think of it like a locked building:
> - You **cannot ask for your key at the reception desk if the reception door is locked** — that's a deadlock.
> - The **Auth and OTP endpoints must stay open** because a user has no JWT token yet. They are in the process of *getting* one.
> - Once they register → verify → login, they receive a JWT, and **every other endpoint from that point onwards is protected**.
>
> These open endpoints are still **not insecure** — they are protected by OTP verification, rate limiting (recommended), and input validation.

---

### 🔑 Authentication — `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/initiate-registration` | 🔓 No Token | Start registration — stores data & sends OTP to email |
| `POST` | `/api/auth/verifyOtpAndRegister` | 🔓 No Token | Verify OTP → create account → return JWT |
| `POST` | `/api/auth/register` | 🔓 No Token | Direct registration without OTP step |
| `POST` | `/api/auth/login` | 🔓 No Token | Login with email/password → returns JWT + sets refresh token cookie |
| `POST` | `/api/auth/refreshToken` | 🔓 No Token | Uses refresh token cookie to issue a new access JWT |
| `POST` | `/api/auth/logout` | 🔓 No Token | Invalidates session in DB and clears refresh token |

> 💡 **Why is `/refreshToken` and `/logout` open?**
> These endpoints use the **HttpOnly cookie** (refresh token) instead of the Authorization header. The JWT filter only reads the `Authorization` header, so these routes are intentionally left open at the filter level — the refresh token in the cookie acts as their credential.

---

### 📧 OTP — `/api/otp`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/otp/otpSent` | 🔓 No Token | Send a 6-digit OTP to the provided email |
| `POST` | `/api/otp/verifyOtp` | 🔓 No Token | Submit and verify the OTP |
| `POST` | `/api/otp/resendOtp` | 🔓 No Token | Resend a fresh OTP (resets timer) |
| `GET` | `/api/otp/check-verification/{email}` | 🔓 No Token | Check if the email is already verified |

> 💡 **Why are OTP endpoints open?** The user does not have an account yet — they are in the middle of creating one. There's no token to validate against. OTP itself is the verification mechanism here.

> OTP config: 6-digit code · expires in **10 minutes** · max **5 attempts** · cached in **Redis**

---

### 👤 User Profile — `/api/user`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/user/getProfileDetails` | 🔒 USER | Fetch current user's profile |
| `PUT` | `/api/user/updateProfileDetails` | 🔒 USER | Update profile info |
| `DELETE` | `/api/user/deleteProfile` | 🔒 USER | Soft-delete account |

---

### 🚗 Activities — `/api/user/activities`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/user/activities/createActivity` | 🔒 USER | Log a new activity (auto-calculates CO₂e) |
| `GET` | `/api/user/activities/getUserActivities` | 🔒 USER | Get all user activities |
| `GET` | `/api/user/activities/paginated?page=0&size=10` | 🔒 USER | Get paginated activities |
| `GET` | `/api/user/activities/{activityId}` | 🔒 USER | Get a specific activity |
| `PUT` | `/api/user/activities/{activityId}` | 🔒 USER | Update an activity |
| `DELETE` | `/api/user/activities/{activityId}` | 🔒 USER | Delete an activity |

**Activity Categories:**
```
TRAVEL      → CAR_PETROL, CAR_DIESEL, CAR_ELECTRIC, MOTORCYCLE, BUS, TRAIN,
               FLIGHT_DOMESTIC, FLIGHT_INTERNATIONAL, BICYCLE, WALKING
ENERGY      → ELECTRICITY_GRID, ELECTRICITY_SOLAR, NATURAL_GAS, HEATING_OIL
PURCHASES   → FOOD_MEAT, FOOD_VEGETARIAN, FOOD_VEGAN, ELECTRONICS,
               CLOTHING, HOUSEHOLD_ITEMS, OTHER_PURCHASES
```

---

### 📊 Dashboard — `/api/user/dashboard`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/user/dashboard/summary?period=monthly` | 🔒 USER | Get emission summary for a period |

**Period options:** `daily` · `weekly` · `monthly`

**Response includes:**
- Total CO₂e emissions
- Breakdown by category (TRAVEL, ENERGY, PURCHASES)
- Comparison with previous period (% change)
- Activity count by category
- Top emission category + suggested improvement area

---

### 🎯 Goals — `/api/user/goals`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/user/goals` | 🔒 USER | Create a carbon reduction goal |
| `GET` | `/api/user/goals` | 🔒 USER | Get all user goals |
| `GET` | `/api/user/goals/active` | 🔒 USER | Get currently active goals |
| `PUT` | `/api/user/goals/{goalId}` | 🔒 USER | Update a goal |
| `DELETE` | `/api/user/goals/{goalId}` | 🔒 USER | Delete a goal |

---

### 💡 Recommendations — `/api/user/recommendations`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/user/recommendations` | 🔒 USER | Get existing recommendations |
| `POST` | `/api/user/recommendations/generate` | 🔒 USER | Generate new recommendations based on activity patterns |

---

### ⚙️ Admin — Emission Factors — `/api/admin/emission-factor`

> Requires `ADMIN` role

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/admin/emission-factor` | 🔒 ADMIN | Create a new emission factor |
| `GET` | `/api/admin/emission-factor` | 🔒 USER/ADMIN | Get all emission factors |
| `GET` | `/api/admin/emission-factor/region/{region}` | 🔒 USER/ADMIN | Get factors by region |
| `GET` | `/api/admin/emission-factor/category/{category}` | 🔒 USER/ADMIN | Get factors by category |
| `DELETE` | `/api/admin/emission-factor/{factorId}` | 🔒 ADMIN | Delete an emission factor |

---

### 🩺 Health Check

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/actuator/health` | 🔓 No Token | Application health status (for load balancers / monitoring tools) |

---

## 🗃️ Data Models

### User
```json
{
  "id": "string",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "region": "IN",
  "roles": ["ROLE_USER"],
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2024-01-01T00:00:00",
  "lastLoginAt": "2024-01-15T10:30:00"
}
```

### Activity
```json
{
  "id": "string",
  "userId": "string",
  "category": "TRAVEL",
  "subType": "CAR_PETROL",
  "quantity": 50.0,
  "unit": "km",
  "co2eEmissions": 9.35,
  "emissionFactorRef": "string",
  "description": "Drive to office",
  "activityDate": "2024-01-15T08:00:00",
  "location": "Mumbai, IN"
}
```

### UserGoal
```json
{
  "id": "string",
  "userId": "string",
  "title": "Reduce travel emissions",
  "goalType": "REDUCE_BY_PERCENTAGE",
  "targetCategory": "TRAVEL",
  "targetValue": 20.0,
  "period": "MONTHLY",
  "currentValue": 8.5,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

### EmissionFactor (Admin-managed)
```json
{
  "id": "string",
  "region": "IN",
  "category": "TRAVEL",
  "subType": "CAR_PETROL",
  "unit": "km",
  "co2eFactor": 0.187,
  "methodology": "DEFRA 2023",
  "source": "Ministry of Environment India",
  "createdBy": "admin@greentrack.com"
}
```

---

## 🔐 Security

### Authentication Flow

```
Registration:
  1. POST /api/auth/initiate-registration  →  Stores user data + sends OTP via SendGrid
  2. POST /api/auth/verifyOtpAndRegister   →  Validates OTP from Redis → creates user → returns JWT

Login:
  1. POST /api/auth/login                  →  Returns access token (JSON) + refresh token (HttpOnly Cookie)
  2. POST /api/auth/refreshToken           →  Reads cookie → validates session → issues new access token
  3. POST /api/auth/logout                 →  Invalidates session in MongoDB

Google OAuth2:
  1. GET /oauth2/authorization/google      →  Spring redirects to Google consent screen
  2. Callback → OAuth2SuccessHandler       →  Auto-creates user if new → generates JWT → redirects to frontend
```

### Token Details
| Token | Expiry | Storage |
|-------|--------|---------|
| Access Token | 15 days | Response body (Authorization header) |
| Refresh Token | 90 days | HttpOnly cookie + MongoDB session |

### Authorization Levels
```
Public           → /api/auth/**, /api/otp/**, /actuator/health, /oauth2/**, /login/**
Authenticated    → /api/user/**  (ROLE_USER or ROLE_ADMIN)
Admin only       → /api/admin/** (ROLE_ADMIN)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- MongoDB Atlas account (or local MongoDB)
- Redis instance
- SendGrid account
- Google Cloud OAuth2 credentials

### 1. Clone the Repository

```
git clone https://github.com/your-username/GreenTrack-Backend.git
cd GreenTrack-Backend
```

### 2. Configure Environment Variables

Copy the example env file and fill in your values:

```
cp .env.example .env
```

> See [Environment Variables](#-environment-variables) section for all required values.

### 3. Run Locally

```
./mvnw spring-boot:run
```

The API will start at: `http://localhost:8080`

### 4. Test the Health Endpoint

```
curl http://localhost:8080/actuator/health
```

---

## 🔧 Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET_KEY` | Secret key for signing JWTs (min 32 chars) | `your-super-secret-key-here-32chars` |
| `MONGODB_URI` | MongoDB Atlas connection string | `mongodb+srv://user:pass@cluster.mongodb.net/greentrack` |
| `SENDGRID_API_KEY` | SendGrid API key for email delivery | `SG.xxxxxxxx` |
| `SENDGRID_SENDER_EMAIL` | Verified sender email in SendGrid | `noreply@greentrack.com` |
| `REDIS_URL` | Redis connection URL | `redis://default:password@host:6379` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | `xxxxxxxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | `GOCSPX-xxxxxxxx` |
| `FRONTEND_REDIRECT_URL` | Base URL of your frontend app | `https://your-frontend.vercel.app` |

---

## 🐳 Docker Deployment

### Using Docker Compose (Recommended for Local Dev)

```bash
# Build and start all services (app + Redis)
docker-compose up --build

# Run in background
docker-compose up -d --build

# Stop services
docker-compose down
```

The `docker-compose.yml` starts:
- **Redis 7** container (internal only, not exposed)
- **GreenTrack API** on port `8080`

### Build Docker Image Only

```bash
docker build -t greentrack-api .
```

The Dockerfile uses a **multi-stage build**:
1. **Stage 1** — Maven build on `eclipse-temurin:21-alpine` (compiles & packages JAR)
2. **Stage 2** — Minimal JRE runtime image (`eclipse-temurin:21-jdk-alpine`) with just the JAR

This keeps the final image lean and secure.

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/greentrack/carbon_tracker_api/
│   │   ├── GreenTrackApplication.java          # App entry point
│   │   │
│   │   ├── controllers/                        # REST Controllers
│   │   │   ├── AuthController.java             # Registration, Login, Token, Logout
│   │   │   ├── OtpController.java              # OTP send/verify/resend
│   │   │   ├── UserController.java             # User profile CRUD
│   │   │   ├── ActivityController.java         # Activity CRUD + pagination
│   │   │   ├── DashboardController.java        # Emission analytics
│   │   │   ├── GoalController.java             # Carbon goal management
│   │   │   ├── RecommendationController.java   # Recommendations
│   │   │   └── EmissionFactorController.java   # Admin emission factors
│   │   │
│   │   ├── services/                           # Service interfaces
│   │   │   └── impl/                           # Service implementations
│   │   │       ├── ActivityServiceImpl.java
│   │   │       ├── AuthServiceImpl.java
│   │   │       ├── DashboardServiceImpl.java
│   │   │       ├── EmissionCalculationServiceImpl.java
│   │   │       ├── RecommendationServiceImpl.java
│   │   │       ├── UserGoalServiceImpl.java
│   │   │       ├── UserServiceImpl.java
│   │   │       ├── OtpServiceImpl.java
│   │   │       └── EmailServiceImpl.java
│   │   │
│   │   ├── entities/                           # MongoDB document models
│   │   │   ├── User.java
│   │   │   ├── Activity.java
│   │   │   ├── EmissionFactor.java
│   │   │   ├── UserGoal.java
│   │   │   ├── Recommendation.java
│   │   │   ├── Session.java
│   │   │   └── enums/                          # ActivityCategory, ActivitySubType, GoalType, etc.
│   │   │
│   │   ├── repositories/                       # Spring Data MongoDB repositories
│   │   ├── dto/                                # Request/Response DTOs
│   │   ├── configs/                            # App, Redis, Security configs
│   │   ├── filter/                             # JwtAuthFilter (per-request JWT validation)
│   │   ├── handlers/                           # OAuth2SuccessHandler
│   │   ├── security/                           # JwtService, SessionService
│   │   ├── advice/                             # GlobalExceptionHandler, ApiResponse wrapper
│   │   └── model/                              # OtpData model
│   │
│   └── resources/
│       └── application.properties              # App config (reads from env vars)
│
├── Dockerfile                                  # Multi-stage production Docker build
├── Dockerfile.dev                              # Dev Docker build
├── docker-compose.yml                          # Local orchestration (app + Redis)
└── pom.xml                                     # Maven dependencies
```

---

## 🔮 Future Improvements

Here's what can be added to make this project even more powerful:

### 🚀 Features
- [ ] **AI-Powered Recommendations** — Integrate OpenAI/Gemini to generate truly intelligent sustainability tips based on user patterns
- [ ] **Carbon Offset Marketplace** — Allow users to purchase verified carbon offsets directly from the app
- [ ] **Social & Leaderboards** — Compare emissions with friends or community challenges
- [ ] **Push Notifications** — Notify users when they're close to exceeding goals
- [ ] **Recurring Activity Templates** — Let users save and repeat common activities (e.g., "Daily Commute")
- [ ] **Export Reports** — PDF/CSV export of monthly emission reports
- [ ] **Webhooks** — Notify external systems when emission milestones are hit

### 🔧 Technical
- [ ] **Swagger / OpenAPI Docs** — Auto-generated interactive API documentation
- [ ] **Rate Limiting** — Protect endpoints from abuse with Bucket4j or Redis-based limiting
- [ ] **Caching Layer** — Cache dashboard summaries in Redis to reduce DB load
- [ ] **Comprehensive Test Suite** — Unit + integration tests with MockMvc and Testcontainers
- [ ] **CI/CD Pipeline** — GitHub Actions for automated testing and deployment
- [ ] **Distributed Tracing** — Add Zipkin/Jaeger for request tracing
- [ ] **Metrics Dashboard** — Expose Prometheus metrics via Actuator for Grafana visualization
- [ ] **API Versioning** — Version APIs (`/api/v1/`) for backward compatibility
- [ ] **Multi-device Session Management** — Let users view and revoke individual device sessions
- [ ] **Database Migrations** — Track schema/data migrations with Mongock

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📬 Contact

**Krishnakant Nagvanshi**

Feel free to reach out via GitHub Issues or connect on [LinkedIn](http://www.linkedin.com/in/krishnakant-nagvanshi/).

---

<div align="center">

**⭐ If you find this project useful, please give it a star!**

*Built with ❤️ for a greener planet 🌱*

</div>






# ⚙️ Job Platform API

The backend REST API for the Job Platform, built with **Spring Boot 3.5** and **Java 17**. Provides secure endpoints for authentication, job management, user profiles, applications, resume handling, and intelligent job recommendations.

---

## 📐 Architecture

The API follows a **layered modular architecture**, organized by business domain:

```
src/main/java/com/raghav/jobplatform/
│
├── JobPlatformApiApplication.java      # Application entry point & DB migrations
│
├── auth/                               # Authentication Module
│   ├── controller/                     # Login & registration endpoints
│   ├── dto/                            # Auth request/response DTOs
│   └── service/                        # Authentication business logic
│
├── jobs/                               # Jobs Module
│   ├── controller/                     # Job CRUD, search & application endpoints
│   ├── dto/                            # Job & application DTOs
│   ├── entity/                         # JPA entities (Job, Application, etc.)
│   ├── model/                          # Domain models & enums
│   ├── repository/                     # Spring Data JPA repositories
│   ├── service/                        # Job & application business logic
│   ├── specification/                  # JPA Specifications for dynamic queries
│   ├── scheduler/                      # Scheduled tasks (job sync, cleanup)
│   ├── client/                         # External API clients
│   └── provider/                       # Data provider abstractions
│
├── user/                               # User Module
│   ├── controller/                     # User profile endpoints
│   ├── dto/                            # User DTOs
│   ├── entity/                         # User JPA entity
│   ├── repository/                     # User repository
│   └── service/                        # User business logic
│
├── config/                             # Configuration
│   ├── SecurityConfig.java             # Spring Security & CORS config
│   ├── JwtService.java                 # JWT generation & validation
│   ├── JwtAuthenticationFilter.java    # JWT request filter
│   ├── CustomUserDetailsService.java   # UserDetailsService implementation
│   ├── RestClientConfig.java           # External HTTP client config
│   └── JobApiProperties.java           # External API config properties
│
└── common/                             # Shared Utilities
    └── GlobalExceptionHandler.java     # Centralized error handling
```

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.5.15 | Application framework |
| **Java** | 17 | Language runtime |
| **Spring Data JPA** | — | ORM & database access |
| **Spring Security** | — | Authentication & authorization |
| **Auth0 java-jwt** | 4.4.0 | JWT token management |
| **jBCrypt** | 0.4 | Password hashing |
| **SpringDoc OpenAPI** | 2.8.16 | Swagger API docs |
| **Spring Boot Actuator** | — | Health & metrics |
| **Spring Boot DevTools** | — | Hot reload during development |
| **Lombok** | — | Boilerplate code reduction |
| **PostgreSQL** | 14+ | Relational database |
| **Maven** | 3.8+ | Build & dependency management |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or later
- **Maven 3.8+** (or use the included `mvnw` wrapper)
- **PostgreSQL 14+** running locally

### 1. Database Setup

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE job_platform;
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/job_platform
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 3. Build & Run

Using the Maven wrapper (no Maven installation required):

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Or with Maven installed:

```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8080**

### 4. Verify

- **Health Check:** http://localhost:8080/actuator/health
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Login & receive JWT | ❌ |

### Jobs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/jobs` | List/search jobs (with filters) | ❌ |
| `GET` | `/api/jobs/{id}` | Get job details | ❌ |
| `POST` | `/api/jobs` | Create a job listing | ✅ |
| `PUT` | `/api/jobs/{id}` | Update a job listing | ✅ |
| `DELETE` | `/api/jobs/{id}` | Delete a job listing | ✅ |

### Applications

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/jobs/{id}/apply` | Apply to a job | ✅ |
| `GET` | `/api/applications` | Get user's applications | ✅ |

### User Profile

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/users/profile` | Get current user profile | ✅ |
| `PUT` | `/api/users/profile` | Update user profile | ✅ |

### Recommendations

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/recommendations` | Get personalized job recommendations | ✅ |

### Saved Jobs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/saved-jobs/{id}` | Save a job | ✅ |
| `GET` | `/api/saved-jobs` | Get saved jobs | ✅ |
| `DELETE` | `/api/saved-jobs/{id}` | Remove saved job | ✅ |

> 📝 **Note:** Full endpoint documentation is available via Swagger UI at `/swagger-ui/index.html`

---

## 🔐 Authentication Flow

```
1. User registers → POST /api/auth/register
2. User logs in   → POST /api/auth/login → Receives JWT token
3. Client stores JWT in localStorage
4. All subsequent requests include: Authorization: Bearer <token>
5. JwtAuthenticationFilter validates token on each request
6. SecurityConfig defines public vs. protected routes
```

---

## 🗃️ Database Schema

The application uses **JPA with `ddl-auto=update`** for schema management. Key entities:

| Entity | Description |
|--------|-------------|
| `User` | User accounts with profile fields (experience, bio, social links, preferences) |
| `Job` | Job listings with details (title, company, location, salary, etc.) |
| `Application` | Job applications linking users to jobs |
| `SavedJob` | Bookmarked jobs for users |

Auto-migration on startup adds profile columns:
- `experience`, `current_role_title`, `bio`
- `linkedin`, `github`, `portfolio`
- `preferred_roles`, `preferred_locations`, `remote_only`
- `salary_range`, `job_types`

---

## 📁 Build Commands

```bash
# Build the project
./mvnw clean package

# Run tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests

# Run the JAR directly
java -jar target/job-platform-api-0.0.1-SNAPSHOT.jar
```

---

## ⚙️ Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | API server port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/job_platform` | Database URL |
| `spring.datasource.username` | `postgres` | DB username |
| `spring.datasource.password` | `0000` | DB password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management strategy |
| `spring.jpa.show-sql` | `true` | Log SQL queries |

---

## 📝 License

This project is for learning and portfolio purposes.

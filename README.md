# ⚙️ Job Platform API

The backend REST API for the Job Platform, built with **Spring Boot 3.5** and **Java 17**. Provides secure endpoints for authentication, job management, user profiles, applications, resume handling, and intelligent job recommendations. It acts as the gateway to the database and coordinates with the Python FastAPI service for AI-driven operations.

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
│   ├── controller/                     # User profile & Resume endpoints
│   ├── dto/                            # User and Resume DTOs
│   ├── entity/                         # User & Resume JPA entities
│   ├── repository/                     # User and Resume repositories
│   └── service/                        # User and Resume business logic
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
    ├── GlobalExceptionHandler.java     # Centralized error handling
    └── ai/                             # AI Client integration for FastAPI communication
        ├── AIClient.java               # Forwards resume files to FastAPI
        ├── AIException.java            # AI-specific error boundaries
        └── dto/                        # FastAPI request/response mapping DTOs
```

---

## 🤖 AI & FastAPI Integration

The backend interacts with the FastAPI machine learning service via Spring's reactive `WebClient`.

1. **Resume Processing:** When a PDF resume is uploaded (`POST /api/resumes`), `ResumeService` calls `AIClient.processResume()`. This transmits the file bytes to FastAPI's `/api/v1/resume/process` endpoint. FastAPI returns parsed profile entities (skills, projects, education) which are saved into relational tables.
2. **Matching Recommendations:** When a client fetches recommendations (`POST /api/recommendations`), `RecommendationOrchestrator` fetches the user's active resume and matching jobs, compiles them into a `RecommendationRequest`, and posts them to FastAPI's `/api/v1/recommendations/generate` endpoint. The scored list and AI match descriptions are mapped and returned to the UI.

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
- **PostgreSQL 14+** running locally (or via Docker pgvector container)

### 1. Database Setup

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE job_platform;
```

### 2. Configure Application

Edit `src/main/resources/application.properties` (or `application.yml`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/job_platform
spring.datasource.username=postgres
spring.datasource.password=your_password
```

For docker settings, environment variables are mapped in `compose.yaml`.

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
| `POST`| `/api/jobs/sync` | Manually run syncer to import jobs | ✅ |

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
| `POST`| `/api/recommendations` | Generate personalized job recommendations | ✅ |

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
| `Resume` | Uploaded PDF documents and parsed skills, summaries, educations, projects |

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
| `spring.datasource.password` | `<your_password>` | DB password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management strategy |
| `spring.jpa.show-sql` | `true` | Log SQL queries |
| `ai.enabled` | `true` | Enable FastAPI calling |
| `ai.base-url` | `http://localhost:8000` | FastAPI base URL |

---

## 📝 License

This project is for learning and portfolio purposes.

# Task Tracker REST API

A production-ready **Task Tracker REST API** developed with **Java**, **Spring Boot 3**, and **MySQL**, adhering to enterprise **Controller–Service–Repository** architecture.

The application allows tracking daily tasks, managing lifecycles (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), prioritizing assignments (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), calculating overdue deadlines, and displaying dashboard productivity metrics.

---

## 🌟 Key Features

- **Controller–Service–Repository Architecture**: Complete separation of web layer, business domain logic, and data access persistence.
- **Full CRUD & Tracking Operations**:
  - `POST /api/tasks` – Create new task with Bean Validation (`@Valid`, `@NotBlank`, `@Size`).
  - `GET /api/tasks` – Retrieve tasks with multi-criteria dynamic filtering (`status`, `priority`, `dueDate`, `search`).
  - `GET /api/tasks/{id}` – Retrieve specific task by ID (returns standard 404 error if not found).
  - `PUT /api/tasks/{id}` – Full task entity update.
  - `PATCH /api/tasks/{id}/status` – Quick status update for daily task progression.
  - `DELETE /api/tasks/{id}` – Delete task by ID.
  - `GET /api/tasks/stats` – Aggregated task tracker metrics (total, completed, pending, overdue counts, completion rate %).
- **Database Support**:
  - **MySQL (Production)**: Fully configured with `mysql-connector-j`, HikariCP connection pooling, and schema initialization script (`schema-mysql.sql`).
  - **In-Memory H2 (Dev / Test)**: Pre-configured with MySQL compatibility mode for instant execution out of the box with zero external dependencies.
- **Global Error Handling**: Centralized `@RestControllerAdvice` formatting errors into structured JSON with HTTP status codes (400, 404, 500).
- **Interactive Web Dashboard**: Built-in modern Single-Page Application (SPA) accessible at `http://localhost:8081/` featuring live metrics, task filters, CRUD modals, and a live REST JSON inspector.
- **Automated Tests**: 16 unit and integration test cases covering controller endpoints, service business logic, and JPA queries using MockMvc and Mockito.

---

## 🏗️ Architecture Overview

```
com.tasktracker.api
├── controller
│   └── TaskController.java           <-- REST endpoints, HTTP status mappings, request validation
├── service
│   ├── TaskService.java              <-- Business logic interface
│   └── impl
│       └── TaskServiceImpl.java      <-- Core business logic, status tracking, transactions
├── repository
│   └── TaskRepository.java           <-- Spring Data JPA repository & JPQL queries
├── model
│   ├── Task.java                     <-- JPA Entity mapped to MySQL 'tasks' table
│   ├── TaskStatus.java               <-- Enum: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
│   └── TaskPriority.java             <-- Enum: LOW, MEDIUM, HIGH, URGENT
├── dto
│   ├── ApiResponse.java              <-- Generic standardized JSON wrapper
│   ├── TaskRequestDto.java           <-- Validated request payload
│   ├── TaskResponseDto.java          <-- Client response model with overdue calculation
│   ├── TaskStatusUpdateDto.java      <-- Lightweight DTO for status progression
│   └── TaskStatsDto.java             <-- Metrics and completion rate statistics
├── exception
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── ErrorResponse.java            <-- Standard error response model
│   └── GlobalExceptionHandler.java   <-- @RestControllerAdvice error handler
└── config
    └── DataInitializer.java          <-- Initial seed data for daily tasks
```

---

## 🗄️ MySQL Database Setup

### 1. Database Creation
If running MySQL locally, create the database and user using MySQL Workbench or the MySQL command line:

```sql
CREATE DATABASE IF NOT EXISTS task_tracker_db;
USE task_tracker_db;
```

A complete table creation script is available at `schema-mysql.sql`:
```sql
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Configure Credentials
Update `src/main/resources/application-mysql.properties` if your MySQL username/password differs from default `root`/`root`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_tracker_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## 🚀 Running the Application

### Option A: Development Mode (Default, H2 with MySQL syntax)
```bash
./mvnw.cmd spring-boot:run
```
Or with packaged JAR:
```bash
java -jar target/task-tracker-api-0.0.1-SNAPSHOT.jar
```
*Port:* `http://localhost:8081`  
*H2 Console:* `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:tasktrackerdb`, User: `sa`, Password: blank)

### Option B: With MySQL Profile
Ensure MySQL Server is running on port 3306, then run:
```bash
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
Or with JAR:
```bash
java -jar target/task-tracker-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql
```

---

## 📡 REST API Endpoints

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/tasks` | List tasks (supports `status`, `priority`, `dueDate`, `search`) | None |
| `GET` | `/api/tasks/{id}` | Get task by ID | None |
| `GET` | `/api/tasks/stats` | Get summary metrics & completion rate | None |
| `POST` | `/api/tasks` | Create a new task | `TaskRequestDto` (JSON) |
| `PUT` | `/api/tasks/{id}` | Update task completely | `TaskRequestDto` (JSON) |
| `PATCH` | `/api/tasks/{id}/status` | Quick update task status | `TaskStatusUpdateDto` (JSON) |
| `DELETE` | `/api/tasks/{id}` | Delete task by ID | None |

---

## 🧪 Testing with JSON Requests

### 1. Create a Task (`POST /api/tasks`)
```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Build Spring Boot REST API",
    "description": "Develop Controller, Service, Repository, and MySQL JPA mappings",
    "priority": "HIGH",
    "status": "PENDING",
    "dueDate": "2026-10-02"
  }'
```
**Response (201 Created):**
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": 7,
    "title": "Build Spring Boot REST API",
    "description": "Develop Controller, Service, Repository, and MySQL JPA mappings",
    "status": "PENDING",
    "priority": "HIGH",
    "dueDate": "2026-10-02",
    "createdAt": "2026-09-30T22:29:23",
    "updatedAt": "2026-09-30T22:29:23",
    "overdue": false
  },
  "timestamp": "2026-09-30T22:29:23"
}
```

### 2. Validation Error Handling (`POST /api/tasks` with invalid payload)
```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{ "title": "" }'
```
**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-09-30T22:29:23",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input payload validation failed",
  "path": "/api/tasks",
  "details": [
    "title: Task title is required and cannot be blank",
    "title: Title must be between 2 and 150 characters"
  ]
}
```

### 3. Update Task Status (`PATCH /api/tasks/{id}/status`)
```bash
curl -X PATCH http://localhost:8081/api/tasks/7/status \
  -H "Content-Type: application/json" \
  -d '{ "status": "COMPLETED" }'
```

### 4. Automated Script
Run the automated test suite script:
```powershell
powershell -ExecutionPolicy Bypass -File .\test-api.ps1
```

Or open `requests.http` in VS Code / IntelliJ IDEA with the REST Client plugin.

---

## 🖥️ Interactive Web Dashboard

When the application is running, navigate to:
```
http://localhost:8081/
```
In your browser to use the interactive dashboard with live task cards, statistics counters, search and filter panels, and real-time JSON inspector!

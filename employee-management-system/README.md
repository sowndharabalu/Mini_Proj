# StaffPulse — Employee Management System

A full-stack, production-ready web application built with **Java**, **Spring Boot**, **Spring Data JPA**, **MySQL**, and a responsive **HTML / CSS / JavaScript** frontend.

---

##  Key Highlights & Requirements Delivered

- **Full CRUD Capabilities:** Add new employees, view full profiles, edit existing records, and delete employees with confirmation dialogs.
- **Spring Boot REST APIs:** Clean layered architecture (`Controller` → `Service` → `Repository` → `Entity`) with Bean Validation and centralized `@RestControllerAdvice` error handling.
- **MySQL & In-Memory Profiles:** Pre-configured for MySQL with Hibernate auto-DDL (`update`), bundled with a quick standalone H2 fallback profile and `db_setup.sql` script.
- **Modern Responsive Frontend:** Single-page dashboard built with modern CSS custom properties, live debounced search, department filtering, KPI metric cards, and CSV export.
- **Comprehensive Automated Tests:** 8/8 unit and integration tests passing (`MockMvc`, validation checks, and CRUD test scenarios).

---

##  Architecture & Tech Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Java 17+, Spring Boot 3.3.4 | Spring Web, Spring Data JPA, Hibernate 6, Bean Validation |
| **Database** | MySQL 8.0 / H2 | Relational schema with indexing on `department` & `status` |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript | Responsive grid/flexbox, Font Awesome 6 icons, Plus Jakarta Sans |
| **Build & Test** | Maven Wrapper (`mvnw`), JUnit 5, MockMvc | Zero-dependency build, full automated integration test suite |

---

##  Project Directory Structure

```
employee-management-system/
│
├── .mvn/wrapper/                     # Maven Wrapper binary & configuration
├── mvnw / mvnw.cmd                   # Maven Wrapper scripts (no mvn install needed)
├── pom.xml                           # Project dependencies and plugins
├── db_setup.sql                      # SQL script for MySQL schema & sample seed data
├── README.md                         # Documentation & API specifications
│
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── EmployeeManagementApplication.java  # Spring Boot Main Entry Point
    │   │   ├── config/
    │   │   │   └── DataInitializer.java            # Automatic seed data loader
    │   │   ├── controller/
    │   │   │   └── EmployeeController.java         # REST Endpoints (/api/employees)
    │   │   ├── entity/
    │   │   │   └── Employee.java                   # JPA Entity with Bean Validation
    │   │   ├── exception/
    │   │   │   ├── DuplicateEmailException.java    # 409 Conflict exception
    │   │   │   ├── ErrorResponse.java              # Structured JSON error DTO
    │   │   │   ├── GlobalExceptionHandler.java     # Centralized @RestControllerAdvice
    │   │   │   └── ResourceNotFoundException.java  # 404 Not Found exception
    │   │   ├── repository/
    │   │   │   └── EmployeeRepository.java         # Spring Data JPA + custom queries
    │   │   └── service/
    │   │       ├── EmployeeService.java            # Business logic interface
    │   │       └── EmployeeServiceImpl.java        # Service implementation & stats
    │   │
    │   └── resources/
    │       ├── application.properties              # Primary MySQL configuration
    │       ├── application-h2.properties           # Optional standalone H2 profile
    │       └── static/                             # Frontend assets (served at /)
    │           ├── index.html                      # UI Dashboard & Modals
    │           ├── style.css                       # Modern CSS stylesheet
    │           └── app.js                          # REST API Client & UI controller
    │
    └── test/
        ├── java/com/example/demo/
        │   ├── EmployeeManagementApplicationTests.java
        │   └── EmployeeControllerTest.java         # MockMvc integration tests
        └── resources/
            └── application.properties              # In-memory test DB configuration
```

---

##  MySQL Database Setup

### Option 1: Automatic Table Creation via Hibernate (Default)
1. Ensure your local MySQL service is running:
   - On Windows: Open `services.msc` and start **MySQL80** (or your MySQL service name), or run MySQL Workbench.
2. In MySQL Workbench or MySQL Shell, create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS employee_db;
   ```
3. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/employee_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

### Option 2: Run the SQL Setup Script Manually
You can execute the bundled `db_setup.sql` script directly in MySQL Workbench or via command line:
```bash
mysql -u root -p < db_setup.sql
```

---

##  How to Run the Application

Navigate to the project root directory:
```powershell
cd C:\Users\Sowndaraya\.gemini\antigravity\scratch\employee-management-system
```

### 1. Run with MySQL (Production Profile)
```powershell
.\mvnw.cmd spring-boot:run
```

### 2. Run with H2 In-Memory Database (Instant Standalone Test)
If MySQL server is stopped or you want to test immediately with zero database configuration:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```
*Or using the pre-built JAR:*
```powershell
java -jar target/employee-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=h2
```

### 3. Open the Application in your Browser
Visit: **[http://localhost:8080/](http://localhost:8080/)**

---

##  REST API Documentation

Base URL: `http://localhost:8080/api/employees`

| Method | Endpoint | Description | Request Body | Response Codes |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/employees` | Retrieve all employees (supports `?search=`, `?department=`, `?status=`) | None | `200 OK` |
| `GET` | `/api/employees/{id}` | Retrieve employee by ID | None | `200 OK`, `404 Not Found` |
| `POST` | `/api/employees` | Create a new employee record | JSON `Employee` | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `PUT` | `/api/employees/{id}` | Update existing employee record | JSON `Employee` | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `DELETE` | `/api/employees/{id}` | Delete employee by ID | None | `200 OK`, `404 Not Found` |
| `GET` | `/api/employees/stats` | Dashboard metrics (total count, payroll, department counts) | None | `200 OK` |

### Sample JSON Request Body (POST / PUT)
```json
{
  "firstName": "Sophia",
  "lastName": "Martinez",
  "email": "sophia.martinez@techcorp.com",
  "phone": "+1-555-0199",
  "department": "Engineering",
  "position": "Frontend Lead",
  "salary": 110000.0,
  "hireDate": "2023-04-15",
  "status": "ACTIVE"
}
```

---

##  Running Automated Tests

Run the full integration test suite:
```powershell
.\mvnw.cmd test
```

Build the self-contained executable JAR:
```powershell
.\mvnw.cmd clean package -DskipTests
```
The output JAR is generated at `target/employee-management-0.0.1-SNAPSHOT.jar`.

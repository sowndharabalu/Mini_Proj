-- =======================================================
-- Task Tracker REST API - MySQL Database Schema
-- Compatible with MySQL 8.0+
-- =======================================================

CREATE DATABASE IF NOT EXISTS task_tracker_db;
USE task_tracker_db;

DROP TABLE IF EXISTS tasks;

CREATE TABLE tasks (
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

-- Sample seed records
INSERT INTO tasks (title, description, status, priority, due_date, created_at, updated_at) VALUES
('Design REST API Architecture', 'Establish Controller-Service-Repository pattern and DTO models', 'COMPLETED', 'HIGH', CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
('Configure MySQL Database Connection', 'Set up tables, JPA entity mappings, and HikariCP connection pool', 'COMPLETED', 'HIGH', CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
('Implement Task CRUD Endpoints', 'Build POST, GET, PUT, PATCH, DELETE endpoints with validation', 'IN_PROGRESS', 'URGENT', CURDATE(), NOW(), NOW()),
('Execute Automated MockMvc Tests', 'Verify JSON payloads, HTTP response codes, and service layer logic', 'IN_PROGRESS', 'HIGH', CURDATE() + INTERVAL 1 DAY, NOW(), NOW()),
('Review Daily Task Tracking Dashboard', 'Inspect statistics, completion rate metrics, and upcoming deadlines', 'PENDING', 'MEDIUM', CURDATE() + INTERVAL 2 DAY, NOW(), NOW()),
('Prepare Production Deployment Checklist', 'Package executable JAR and configure MySQL environment credentials', 'PENDING', 'LOW', CURDATE() + INTERVAL 5 DAY, NOW(), NOW());

-- ========================================================
-- Employee Management System - MySQL Setup Script
-- ========================================================

-- 1. Create Database if it does not exist
CREATE DATABASE IF NOT EXISTS employee_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE employee_db;

-- 2. Create Employees Table (Hibernate will also auto-create this if ddl-auto=update)
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    position VARCHAR(100) NOT NULL,
    salary DOUBLE NOT NULL,
    hire_date DATE NOT NULL,
    phone VARCHAR(25),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    INDEX idx_emp_dept (department),
    INDEX idx_emp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Insert Initial Sample Data (optional, DataInitializer also provides this)
INSERT INTO employees (first_name, last_name, email, department, position, salary, hire_date, phone, status)
VALUES 
('Alice', 'Johnson', 'alice.johnson@techcorp.com', 'Engineering', 'Senior Full Stack Engineer', 105000.00, '2022-03-15', '+1-555-0101', 'ACTIVE'),
('Bob', 'Smith', 'bob.smith@techcorp.com', 'Engineering', 'DevOps Lead', 115000.00, '2021-06-01', '+1-555-0102', 'ACTIVE'),
('Catherine', 'Lee', 'catherine.lee@techcorp.com', 'Human Resources', 'HR Manager', 85000.00, '2020-11-20', '+1-555-0103', 'ACTIVE'),
('David', 'Brown', 'david.brown@techcorp.com', 'Finance', 'Financial Analyst', 78000.00, '2023-01-10', '+1-555-0104', 'ACTIVE'),
('Emma', 'Davis', 'emma.davis@techcorp.com', 'Marketing', 'Marketing Director', 95000.00, '2019-08-05', '+1-555-0105', 'ACTIVE'),
('Franklin', 'Miller', 'franklin.miller@techcorp.com', 'Operations', 'Operations Coordinator', 68000.00, '2024-02-18', '+1-555-0106', 'ON_LEAVE'),
('Grace', 'Wilson', 'grace.wilson@techcorp.com', 'Product', 'Senior Product Manager', 120000.00, '2021-09-12', '+1-555-0107', 'ACTIVE'),
('Henry', 'Taylor', 'henry.taylor@techcorp.com', 'Engineering', 'Junior Backend Developer', 72000.00, '2024-05-02', '+1-555-0108', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

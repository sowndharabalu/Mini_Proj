package com.example.demo.config;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    public DataInitializer(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            List<Employee> initialEmployees = Arrays.asList(
                    new Employee("Alice", "Johnson", "alice.johnson@techcorp.com", "Engineering",
                            "Senior Full Stack Engineer", 105000.0, LocalDate.of(2022, 3, 15), "+1-555-0101", "ACTIVE"),
                    new Employee("Bob", "Smith", "bob.smith@techcorp.com", "Engineering",
                            "DevOps Lead", 115000.0, LocalDate.of(2021, 6, 1), "+1-555-0102", "ACTIVE"),
                    new Employee("Catherine", "Lee", "catherine.lee@techcorp.com", "Human Resources",
                            "HR Manager", 85000.0, LocalDate.of(2020, 11, 20), "+1-555-0103", "ACTIVE"),
                    new Employee("David", "Brown", "david.brown@techcorp.com", "Finance",
                            "Financial Analyst", 78000.0, LocalDate.of(2023, 1, 10), "+1-555-0104", "ACTIVE"),
                    new Employee("Emma", "Davis", "emma.davis@techcorp.com", "Marketing",
                            "Marketing Director", 95000.0, LocalDate.of(2019, 8, 5), "+1-555-0105", "ACTIVE"),
                    new Employee("Franklin", "Miller", "franklin.miller@techcorp.com", "Operations",
                            "Operations Coordinator", 68000.0, LocalDate.of(2024, 2, 18), "+1-555-0106", "ON_LEAVE"),
                    new Employee("Grace", "Wilson", "grace.wilson@techcorp.com", "Product",
                            "Senior Product Manager", 120000.0, LocalDate.of(2021, 9, 12), "+1-555-0107", "ACTIVE"),
                    new Employee("Henry", "Taylor", "henry.taylor@techcorp.com", "Engineering",
                            "Junior Backend Developer", 72000.0, LocalDate.of(2024, 5, 2), "+1-555-0108", "ACTIVE")
            );

            employeeRepository.saveAll(initialEmployees);
            System.out.println(">>> Sample employees successfully initialized into database.");
        }
    }
}

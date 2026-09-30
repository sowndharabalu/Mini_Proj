package com.example.demo.service;

import com.example.demo.entity.Employee;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee createEmployee(Employee employee);

    Employee updateEmployee(Long id, Employee employeeDetails);

    void deleteEmployee(Long id);

    List<Employee> searchEmployees(String keyword);

    List<Employee> filterByDepartment(String department);

    List<Employee> filterByStatus(String status);

    Map<String, Object> getDashboardStats();
}

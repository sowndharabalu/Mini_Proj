package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    @Override
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException("An employee with email '" + employee.getEmail() + "' already exists");
        }
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // If email has changed, check for conflict
        if (!existingEmployee.getEmail().equalsIgnoreCase(employeeDetails.getEmail())) {
            Optional<Employee> duplicate = employeeRepository.findByEmail(employeeDetails.getEmail());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new DuplicateEmailException("An employee with email '" + employeeDetails.getEmail() + "' already exists");
            }
        }

        existingEmployee.setFirstName(employeeDetails.getFirstName());
        existingEmployee.setLastName(employeeDetails.getLastName());
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setDepartment(employeeDetails.getDepartment());
        existingEmployee.setPosition(employeeDetails.getPosition());
        existingEmployee.setSalary(employeeDetails.getSalary());
        existingEmployee.setHireDate(employeeDetails.getHireDate());
        existingEmployee.setPhone(employeeDetails.getPhone());
        existingEmployee.setStatus(employeeDetails.getStatus());

        return employeeRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }
        return employeeRepository.searchEmployees(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> filterByDepartment(String department) {
        if (department == null || department.trim().equalsIgnoreCase("ALL")) {
            return getAllEmployees();
        }
        return employeeRepository.findByDepartmentIgnoreCase(department.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> filterByStatus(String status) {
        if (status == null || status.trim().equalsIgnoreCase("ALL")) {
            return getAllEmployees();
        }
        return employeeRepository.findByStatusIgnoreCase(status.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        List<Employee> all = employeeRepository.findAll();
        long total = all.size();
        long active = all.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
        long onLeave = all.stream().filter(e -> "ON_LEAVE".equalsIgnoreCase(e.getStatus())).count();
        long inactive = all.stream().filter(e -> "INACTIVE".equalsIgnoreCase(e.getStatus())).count();
        double totalPayroll = all.stream().mapToDouble(e -> e.getSalary() != null ? e.getSalary() : 0.0).sum();
        double avgSalary = total > 0 ? (totalPayroll / total) : 0.0;

        Map<String, Long> departmentCount = all.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", total);
        stats.put("activeEmployees", active);
        stats.put("onLeaveEmployees", onLeave);
        stats.put("inactiveEmployees", inactive);
        stats.put("totalPayroll", Math.round(totalPayroll * 100.0) / 100.0);
        stats.put("averageSalary", Math.round(avgSalary * 100.0) / 100.0);
        stats.put("departmentBreakdown", departmentCount);

        return stats;
    }
}

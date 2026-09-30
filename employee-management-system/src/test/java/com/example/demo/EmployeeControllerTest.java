package com.example.demo;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
    }

    @Test
    void testCreateEmployee() throws Exception {
        Employee employee = new Employee(
                "Jane", "Doe", "jane.doe@example.com", "Engineering",
                "Backend Engineer", 88000.0, LocalDate.of(2023, 5, 10), "+1-555-1234", "ACTIVE"
        );

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.department", is("Engineering")));
    }

    @Test
    void testCreateEmployeeValidationFailure() throws Exception {
        // Missing required fields
        Employee invalid = new Employee();
        invalid.setFirstName(""); // Blank name

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void testGetAllEmployees() throws Exception {
        Employee emp = new Employee(
                "Mark", "Ruffalo", "mark.ruffalo@example.com", "Marketing",
                "Content Strategist", 75000.0, LocalDate.of(2022, 1, 15), null, "ACTIVE"
        );
        employeeRepository.save(emp);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Mark")));
    }

    @Test
    void testGetEmployeeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        Employee saved = employeeRepository.save(new Employee(
                "Sarah", "Connor", "sarah@example.com", "Operations",
                "Coordinator", 65000.0, LocalDate.of(2023, 2, 1), null, "ACTIVE"
        ));

        saved.setPosition("Operations Manager");
        saved.setSalary(82000.0);

        mockMvc.perform(put("/api/employees/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position", is("Operations Manager")))
                .andExpect(jsonPath("$.salary", is(82000.0)));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        Employee saved = employeeRepository.save(new Employee(
                "Tom", "Hanks", "tom.hanks@example.com", "Finance",
                "Accountant", 70000.0, LocalDate.of(2021, 4, 1), null, "ACTIVE"
        ));

        mockMvc.perform(delete("/api/employees/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(get("/api/employees/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDashboardStats() throws Exception {
        employeeRepository.save(new Employee(
                "Alex", "Turner", "alex@example.com", "Engineering",
                "Lead Architect", 130000.0, LocalDate.of(2020, 1, 1), null, "ACTIVE"
        ));

        mockMvc.perform(get("/api/employees/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees", is(1)))
                .andExpect(jsonPath("$.activeEmployees", is(1)))
                .andExpect(jsonPath("$.totalPayroll", is(130000.0)));
    }
}

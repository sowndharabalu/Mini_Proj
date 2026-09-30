package com.tasktracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.api.dto.TaskRequestDto;
import com.tasktracker.api.dto.TaskStatusUpdateDto;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for Task Tracker REST API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/tasks - Successfully create a daily task")
    void testCreateTask_Success() throws Exception {
        TaskRequestDto request = new TaskRequestDto(
                "Write Integration Test Cases",
                "Verify API behavior with valid and invalid JSON requests",
                TaskStatus.PENDING,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(2)
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Task created successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("Write Integration Test Cases")))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.priority", is("HIGH")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/tasks - Validation error on blank title")
    void testCreateTask_ValidationError_BlankTitle() throws Exception {
        TaskRequestDto invalidRequest = new TaskRequestDto(
                " ", // Blank title
                "Some description",
                TaskStatus.PENDING,
                TaskPriority.LOW,
                LocalDate.now()
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details", hasItem(containsString("title"))));
    }

    @Test
    @DisplayName("GET /api/tasks - Retrieve all tasks")
    void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/tasks - Filter tasks by status")
    void testGetTasks_WithFilter() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[*].status", everyItem(is("COMPLETED"))));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Successfully retrieve task by ID")
    void testGetTaskById_Success() throws Exception {
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Return 404 when task not found")
    void testGetTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Task not found with id: '999999'")));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Fully update an existing task")
    void testUpdateTask_Success() throws Exception {
        TaskRequestDto updateDto = new TaskRequestDto(
                "Updated Task Title",
                "Updated description details",
                TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT,
                LocalDate.now().plusDays(5)
        );

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Updated Task Title")))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.priority", is("URGENT")));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status - Quick update task status for tracking")
    void testUpdateTaskStatus_Success() throws Exception {
        TaskStatusUpdateDto statusDto = new TaskStatusUpdateDto(TaskStatus.COMPLETED);

        mockMvc.perform(patch("/api/tasks/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("GET /api/tasks/stats - Retrieve task tracker metrics")
    void testGetTaskStatistics() throws Exception {
        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalTasks", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.completionRate", notNullValue()));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Delete an existing task")
    void testDeleteTask_Success() throws Exception {
        // First create a temporary task to delete
        TaskRequestDto temp = new TaskRequestDto("Temp Task To Delete", "Desc", TaskStatus.PENDING, TaskPriority.LOW, null);
        String responseStr = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(temp)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer tempId = objectMapper.readTree(responseStr).path("data").path("id").asInt();

        // Now delete it
        mockMvc.perform(delete("/api/tasks/" + tempId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Task deleted successfully")));

        // Verify it is gone
        mockMvc.perform(get("/api/tasks/" + tempId))
                .andExpect(status().isNotFound());
    }
}

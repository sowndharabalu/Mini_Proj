package com.tasktracker.api.controller;

import com.tasktracker.api.dto.ApiResponse;
import com.tasktracker.api.dto.TaskRequestDto;
import com.tasktracker.api.dto.TaskResponseDto;
import com.tasktracker.api.dto.TaskStatsDto;
import com.tasktracker.api.dto.TaskStatusUpdateDto;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import com.tasktracker.api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller exposing endpoints for Task Tracker operations.
 * Base route: /api/tasks
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * POST /api/tasks
     * Create a new task.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@Valid @RequestBody TaskRequestDto dto) {
        TaskResponseDto created = taskService.createTask(dto);
        return new ResponseEntity<>(
                ApiResponse.success("Task created successfully", created),
                HttpStatus.CREATED
        );
    }

    /**
     * GET /api/tasks
     * Retrieve tasks with optional filtering by status, priority, due date, or search term.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) String search) {

        List<TaskResponseDto> tasks = taskService.getAllTasks(status, priority, dueDate, search);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    /**
     * GET /api/tasks/stats
     * Retrieve tracking statistics (totals, completion rate, overdue counts).
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TaskStatsDto>> getTaskStatistics() {
        TaskStatsDto stats = taskService.getTaskStatistics();
        return ResponseEntity.ok(ApiResponse.success("Task statistics retrieved successfully", stats));
    }

    /**
     * GET /api/tasks/{id}
     * Retrieve a specific task by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTaskById(@PathVariable Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    /**
     * PUT /api/tasks/{id}
     * Update an existing task completely.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto dto) {

        TaskResponseDto updated = taskService.updateTask(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updated));
    }

    /**
     * PATCH /api/tasks/{id}/status
     * Quick status update for task tracking (e.g. IN_PROGRESS -> COMPLETED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateDto dto) {

        TaskResponseDto updated = taskService.updateTaskStatus(id, dto.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", updated));
    }

    /**
     * DELETE /api/tasks/{id}
     * Delete a task by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}

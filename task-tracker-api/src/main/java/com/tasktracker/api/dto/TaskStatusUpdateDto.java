package com.tasktracker.api.dto;

import com.tasktracker.api.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating task status (e.g. tracking task progression).
 */
public class TaskStatusUpdateDto {

    @NotNull(message = "Task status is required (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)")
    private TaskStatus status;

    public TaskStatusUpdateDto() {
    }

    public TaskStatusUpdateDto(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}

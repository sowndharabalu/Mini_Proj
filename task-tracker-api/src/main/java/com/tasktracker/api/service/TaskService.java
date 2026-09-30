package com.tasktracker.api.service;

import com.tasktracker.api.dto.TaskRequestDto;
import com.tasktracker.api.dto.TaskResponseDto;
import com.tasktracker.api.dto.TaskStatsDto;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Task business logic and operations.
 */
public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto dto);

    List<TaskResponseDto> getAllTasks(TaskStatus status, TaskPriority priority, LocalDate dueDate, String search);

    TaskResponseDto getTaskById(Long id);

    TaskResponseDto updateTask(Long id, TaskRequestDto dto);

    TaskResponseDto updateTaskStatus(Long id, TaskStatus status);

    void deleteTask(Long id);

    TaskStatsDto getTaskStatistics();
}

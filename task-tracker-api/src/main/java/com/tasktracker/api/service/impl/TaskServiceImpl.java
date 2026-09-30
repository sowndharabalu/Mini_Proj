package com.tasktracker.api.service.impl;

import com.tasktracker.api.dto.TaskRequestDto;
import com.tasktracker.api.dto.TaskResponseDto;
import com.tasktracker.api.dto.TaskStatsDto;
import com.tasktracker.api.exception.ResourceNotFoundException;
import com.tasktracker.api.model.Task;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import com.tasktracker.api.repository.TaskRepository;
import com.tasktracker.api.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation containing core business logic for Task management.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponseDto createTask(TaskRequestDto dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(dto.getDueDate());

        Task savedTask = taskRepository.save(task);
        return TaskResponseDto.fromEntity(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks(TaskStatus status, TaskPriority priority, LocalDate dueDate, String search) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<Task> tasks = taskRepository.findWithFilters(status, priority, dueDate, cleanSearch);
        return tasks.stream()
                .map(TaskResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return TaskResponseDto.fromEntity(task);
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        existingTask.setTitle(dto.getTitle().trim());
        existingTask.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        if (dto.getStatus() != null) {
            existingTask.setStatus(dto.getStatus());
        }
        if (dto.getPriority() != null) {
            existingTask.setPriority(dto.getPriority());
        }
        existingTask.setDueDate(dto.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);
        return TaskResponseDto.fromEntity(updatedTask);
    }

    @Override
    public TaskResponseDto updateTaskStatus(Long id, TaskStatus status) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        existingTask.setStatus(status);
        Task updatedTask = taskRepository.save(existingTask);
        return TaskResponseDto.fromEntity(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        taskRepository.delete(existingTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatsDto getTaskStatistics() {
        long total = taskRepository.count();
        long pending = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgress = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completed = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long cancelled = taskRepository.countByStatus(TaskStatus.CANCELLED);
        long overdue = taskRepository.countOverdueTasks(LocalDate.now(), TaskStatus.COMPLETED, TaskStatus.CANCELLED);

        return new TaskStatsDto(total, pending, inProgress, completed, cancelled, overdue);
    }
}

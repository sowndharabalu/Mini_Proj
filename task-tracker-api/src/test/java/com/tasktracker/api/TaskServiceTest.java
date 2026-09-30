package com.tasktracker.api;

import com.tasktracker.api.dto.TaskRequestDto;
import com.tasktracker.api.dto.TaskResponseDto;
import com.tasktracker.api.dto.TaskStatsDto;
import com.tasktracker.api.exception.ResourceNotFoundException;
import com.tasktracker.api.model.Task;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import com.tasktracker.api.repository.TaskRepository;
import com.tasktracker.api.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying business logic in TaskServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Test Task", "Sample description", TaskStatus.PENDING, TaskPriority.HIGH, LocalDate.now());
        sampleTask.setId(10L);
    }

    @Test
    @DisplayName("createTask should map fields and persist entity")
    void testCreateTask() {
        TaskRequestDto dto = new TaskRequestDto("New Task", "Description", TaskStatus.IN_PROGRESS, TaskPriority.URGENT, LocalDate.now());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TaskResponseDto result = taskService.createTask(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.URGENT);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("getTaskById should return task when found")
    void testGetTaskById_Found() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));

        TaskResponseDto result = taskService.getTaskById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("getTaskById should throw ResourceNotFoundException when missing")
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: '999'");
    }

    @Test
    @DisplayName("updateTaskStatus should change status and save")
    void testUpdateTaskStatus() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponseDto result = taskService.updateTaskStatus(10L, TaskStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("getTaskStatistics should aggregate metrics properly")
    void testGetTaskStatistics() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(4L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.CANCELLED)).thenReturn(1L);
        when(taskRepository.countOverdueTasks(any(LocalDate.class), eq(TaskStatus.COMPLETED), eq(TaskStatus.CANCELLED))).thenReturn(1L);

        TaskStatsDto stats = taskService.getTaskStatistics();

        assertThat(stats.getTotalTasks()).isEqualTo(10L);
        assertThat(stats.getPendingTasks()).isEqualTo(4L);
        assertThat(stats.getCompletedTasks()).isEqualTo(3L);
        assertThat(stats.getCompletionRate()).isEqualTo(30.0);
    }
}

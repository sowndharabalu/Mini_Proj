package com.tasktracker.api.repository;

import com.tasktracker.api.model.Task;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Repository for Task entities interacting with the MySQL database.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByDueDate(LocalDate dueDate);

    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<Task> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Flexible multi-criteria search and filtering for tasks.
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:dueDate IS NULL OR t.dueDate = :dueDate) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.dueDate ASC NULLS LAST, t.createdAt DESC")
    List<Task> findWithFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("dueDate") LocalDate dueDate,
            @Param("search") String search
    );

    long countByStatus(TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status <> :completedStatus AND t.status <> :cancelledStatus")
    long countOverdueTasks(
            @Param("today") LocalDate today,
            @Param("completedStatus") TaskStatus completedStatus,
            @Param("cancelledStatus") TaskStatus cancelledStatus
    );
}

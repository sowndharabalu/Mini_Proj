package com.tasktracker.api.config;

import com.tasktracker.api.model.Task;
import com.tasktracker.api.model.TaskPriority;
import com.tasktracker.api.model.TaskStatus;
import com.tasktracker.api.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * Pre-populates the database with sample tasks on initial startup if empty.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final TaskRepository taskRepository;

    public DataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        if (taskRepository.count() == 0) {
            log.info("Database is empty. Seeding initial daily tasks for Task Tracker...");

            LocalDate today = LocalDate.now();

            List<Task> sampleTasks = List.of(
                    new Task(
                            "Design REST API Architecture",
                            "Establish Controller–Service–Repository structure and define DTOs and validation rules.",
                            TaskStatus.COMPLETED,
                            TaskPriority.HIGH,
                            today.minusDays(2)
                    ),
                    new Task(
                            "Configure MySQL Database Connection",
                            "Set up MySQL database tables, Hibernate JPA mappings, and HikariCP connection pool.",
                            TaskStatus.COMPLETED,
                            TaskPriority.HIGH,
                            today.minusDays(1)
                    ),
                    new Task(
                            "Implement Task CRUD Endpoints",
                            "Build and test POST, GET, PUT, PATCH, and DELETE endpoints with robust error handling.",
                            TaskStatus.IN_PROGRESS,
                            TaskPriority.URGENT,
                            today
                    ),
                    new Task(
                            "Execute Automated MockMvc Tests",
                            "Verify JSON requests, status codes, DTO validations, and edge cases.",
                            TaskStatus.IN_PROGRESS,
                            TaskPriority.HIGH,
                            today.plusDays(1)
                    ),
                    new Task(
                            "Review Daily Task Tracking Dashboard",
                            "Inspect statistics, task completion rates, and upcoming deadlines.",
                            TaskStatus.PENDING,
                            TaskPriority.MEDIUM,
                            today.plusDays(2)
                    ),
                    new Task(
                            "Prepare Production Deployment Checklist",
                            "Document environment variables, MySQL user permissions, and jar packaging.",
                            TaskStatus.PENDING,
                            TaskPriority.LOW,
                            today.plusDays(5)
                    )
            );

            taskRepository.saveAll(sampleTasks);
            log.info("Successfully seeded {} sample tasks.", sampleTasks.size());
        } else {
            log.info("Database already contains tasks. Skipping seeding.");
        }
    }
}

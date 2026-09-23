package org.example.taskflow.task;

import org.example.taskflow.task.dto.TaskRequest;
import org.example.taskflow.task.dto.TaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class TaskIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateAndGetTask() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Integration test");
        request.setDescription("Test with real PostgreSQL");

        TaskResponse created = taskService.createTask(request);

        assertNotNull(created.getId());
        assertEquals("Integration test", created.getTitle());

        TaskResponse found = taskService.getTask(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Integration test", found.getTitle());
        assertEquals(
                "Test with real PostgreSQL",
                found.getDescription()
        );
    }

    @Test
    void shouldGetAllTasks() {
        TaskRequest firstRequest = new TaskRequest();
        firstRequest.setTitle("First task");
        firstRequest.setDescription("First description");

        TaskRequest secondRequest = new TaskRequest();
        secondRequest.setTitle("Second task");
        secondRequest.setDescription("Second description");

        taskService.createTask(firstRequest);
        taskService.createTask(secondRequest);

        List<TaskResponse> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        assertEquals("First task", tasks.get(0).getTitle());
        assertEquals("Second task", tasks.get(1).getTitle());
    }

    @Test
    void shouldUpdateTask() {
        TaskRequest createRequest = new TaskRequest();
        createRequest.setTitle("Original title");
        createRequest.setDescription("Original description");

        TaskResponse created = taskService.createTask(createRequest);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated title");
        updateRequest.setDescription("Updated description");

        TaskResponse updated =
                taskService.updateTask(created.getId(), updateRequest);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated title", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void shouldDeleteTask() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task to delete");
        request.setDescription("This task will be deleted");

        TaskResponse created = taskService.createTask(request);

        taskService.deleteTask(created.getId());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTask(created.getId())
        );
    }
}
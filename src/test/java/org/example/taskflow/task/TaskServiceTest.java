package org.example.taskflow.task;

import org.example.taskflow.redis.RedisService;
import org.example.taskflow.task.dto.TaskRequest;
import org.example.taskflow.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTask_shouldReturnTask_whenTaskExist() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test task");
        task.setDescription("Description");

        when(redisService.getTask(1L)).thenReturn(null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTask(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test task", result.getTitle());
    }

    @Test
    void getTask_shouldThrowException_whenDoesNotExist() {
        when(redisService.getTask(1L)).thenReturn(null);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTask(1L)
        );
    }

    @Test
    void createTask_shouldSaveTask() {

        TaskRequest request = new TaskRequest();
        request.setTitle("Learn Mockito");
        request.setDescription("Write unit tests");

        Task savedTask = new Task();
        savedTask.setTitle("Learn Mockito");
        savedTask.setDescription("Write unit tests");

        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
                .thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertEquals("Learn Mockito", response.getTitle());
        assertEquals("Write unit tests", response.getDescription());
    }

    @Test
    void updateTask_shouldUpdateTaskAndInvalidateCache() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Old title");
        task.setDescription("Old description");

        TaskRequest request = new TaskRequest();
        request.setTitle("New title");
        request.setDescription("New description");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse result = taskService.updateTask(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("New title", result.getTitle());
        assertEquals("New description", result.getDescription());

        verify(taskRepository).save(task);
        verify(redisService).deleteTask(1L);
    }

    @Test
    void deleteTask_shouldDeleteTaskAndInvalidateCache() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task");
        task.setDescription("Description");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
        verify(redisService).deleteTask(1L);
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task firstTask = new Task();
        firstTask.setTitle("First task");
        firstTask.setDescription("First description");

        Task secondTask = new Task();
        secondTask.setTitle("Second task");
        secondTask.setDescription("Second description");

        when(taskRepository.findAll())
                .thenReturn(List.of(firstTask, secondTask));

        List<TaskResponse> response = taskService.getAllTasks();

        assertEquals(2, response.size());
        assertEquals("First task", response.get(0).getTitle());
        assertEquals("Second task", response.get(1).getTitle());

        verify(taskRepository).findAll();
    }

    @Test
    void getTask_shouldReturnCachedTask_withoutCallingRepository() {
        TaskResponse cachedTask = new TaskResponse(
                1L,
                "Cached task",
                "From Redis"
        );

        when(redisService.getTask(1L)).thenReturn(cachedTask);

        TaskResponse result = taskService.getTask(1L);

        assertEquals(1L, result.getId());
        assertEquals("Cached task", result.getTitle());
        assertEquals("From Redis", result.getDescription());

        verify(taskRepository, never()).findById(1L);
    }
}
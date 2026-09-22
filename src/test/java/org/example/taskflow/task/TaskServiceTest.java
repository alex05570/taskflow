package org.example.taskflow.task;

import org.example.taskflow.task.dto.TaskRequest;
import org.example.taskflow.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTask_shouldReturnTask_whenTaskExist() {
        Task task = new Task();
        task.setTitle("Learn Spring");
        task.setDescription("Study Spring Boot");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(1L);

        assertEquals("Learn Spring", response.getTitle());
        assertEquals("Study Spring Boot", response.getDescription());
    }

    @Test
    void getTask_shouldThrowException_whenDoesNotExist() {

        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTask(999L)
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
    void updateTask_shouldUpdateTask_whenTaskExists() {
        Task task = new Task();
        task.setTitle("Old title");
        task.setDescription("Old description");

        TaskRequest request = new TaskRequest();
        request.setTitle("New title");
        request.setDescription("New description");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        TaskResponse response = taskService.updateTask(1L, request);

        assertEquals("New title", response.getTitle());
        assertEquals("New description", response.getDescription());

        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_shouldDeleteTask_whenTaskExists() {
        Task task = new Task();
        task.setTitle("Task to delete");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
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
}
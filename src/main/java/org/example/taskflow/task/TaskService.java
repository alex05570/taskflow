package org.example.taskflow.task;

import org.example.taskflow.redis.RedisService;
import org.example.taskflow.task.dto.TaskRequest;
import org.example.taskflow.task.dto.TaskResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final RedisService redisService;

    public TaskService(TaskRepository taskRepository, RedisService redisService) {
        this.taskRepository = taskRepository;
        this.redisService = redisService;
    }

    public TaskResponse getTask(Long id) {
        TaskResponse cachedTask = redisService.getTask(id);

        if (cachedTask != null) {
            return cachedTask;
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskResponse response = toResponse(task);

        redisService.setTask(id, response);

        return response;
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task savedTask = taskRepository.save(task);

        return new TaskResponse(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription()
        );
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task updateTask =  taskRepository.save(task);

        redisService.deleteTask(id);

        return toResponse(updateTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
        redisService.deleteTask(id);
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription()
        );
    }
}

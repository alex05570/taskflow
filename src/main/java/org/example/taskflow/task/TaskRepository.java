package org.example.taskflow.task;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository {

    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 1;

    public Task findById(long id) {
        for (Task t : tasks) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Task> findAll() {
        return tasks;
    }

    public Task save(Task task) {
        task.setId(nextId++);
        tasks.add(task);
        return task;
    }
}
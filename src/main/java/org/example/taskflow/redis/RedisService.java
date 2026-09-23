package org.example.taskflow.redis;


import tools.jackson.core.JacksonException;
import org.example.taskflow.task.dto.TaskResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;


@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void setTask(Long id, TaskResponse task) {
        try {
            String json = objectMapper.writeValueAsString(task);

            redisTemplate.opsForValue().set(
                    "task:" + id,
                    json,
                    Duration.ofMinutes(10)
            );
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize task", e);
        }
    }

    public TaskResponse getTask(Long id) {
        String json = redisTemplate.opsForValue().get("task:" + id);

        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, TaskResponse.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize task", e);
        }
    }

    public void deleteTask(Long id) {
        redisTemplate.delete("task:" + id);
    }
}
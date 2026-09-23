package org.example.taskflow.redis;


import org.example.taskflow.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void shouldSetAndGetTask() {
        TaskResponse task = new TaskResponse(
                100L,
                "Learn Redis",
                "Understand caching"
        );

        redisService.setTask(100L, task);

        TaskResponse cachedTask = redisService.getTask(100L);

        assertNotNull(cachedTask);
        assertEquals(100L, cachedTask.getId());
        assertEquals("Learn Redis", cachedTask.getTitle());
        assertEquals("Understand caching", cachedTask.getDescription());
    }

    @Test
    void shouldReturnNullWhenTaskIsNotCached() {
        TaskResponse task = redisService.getTask(999999L);

        assertNull(task);
    }

    @Test
    void shouldDeleteTask() {
        TaskResponse task = new TaskResponse(
                200L,
                "Temporary task",
                "Will be deleted"
        );

        redisService.setTask(200L, task);
        redisService.deleteTask(200L);

        assertNull(redisService.getTask(200L));
    }

    @Test
    void shouldSetExpirationForTask() {
        TaskResponse task = new TaskResponse(
                300L,
                "TTL task",
                "Should expire"
        );

        redisService.setTask(300L, task);

        Long ttl = redisTemplate.getExpire("task:300");

        assertNotNull(ttl);
        assertTrue(ttl > 0);
        assertTrue(ttl <= 600);
    }
}
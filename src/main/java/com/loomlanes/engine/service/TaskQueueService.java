package com.loomlanes.engine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loomlanes.engine.model.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskQueueService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRIORITY_QUEUE_KEY = "tasks:priority_queue";

    public void enqueue(TaskRequest request) {
        try {
            String taskJson = objectMapper.writeValueAsString(request);
            redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, taskJson, request.getPriority());

            log.info("📥 Task {} buffered in Redis (Priority: {})", request.getTaskId(), request.getPriority());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize task: {}", request.getTaskId());
            throw new RuntimeException("Serialization error");
        }
    }
}
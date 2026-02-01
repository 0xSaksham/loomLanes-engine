package com.loomlanes.engine.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loomlanes.engine.model.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskWorker {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_KEY = "task:exec:";

    @KafkaListener(topics = "task-execution-topic", groupId = "loom-lanes-workers")
    public void handleTask(String message){
        try {
            TaskRequest task = objectMapper.readValue(message, TaskRequest.class);
            String lockKey = IDEMPOTENCY_KEY + task.getTaskId();

            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(lockKey, "PROCESSING", Duration.ofMinutes(10));

            if (Boolean.FALSE.equals(isNew)){
                log.warn("Task {} is already being processed. Skipping.", task.getTaskId());
                return;
            }

            process(task);

            redisTemplate.opsForValue().set(lockKey, "COMPLETED", Duration.ofHours(1));
            log.info("Task {} completed successfully.", task.getTaskId());
        }
        catch (Exception e){
            log.error("Error processing task: {}", e.getMessage());
        }
    }

    private void process(TaskRequest task) throws InterruptedException{
        log.info("Executing Task: {} [Type: {}] on Thread: {}", task.getTaskId(), task.getType(), Thread.currentThread());

        Thread.sleep(2000);
    }
}
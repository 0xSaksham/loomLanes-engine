package com.loomlanes.engine.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loomlanes.engine.model.TaskRequest;
import com.loomlanes.engine.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskWorker {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String IDEMPOTENCY_KEY = "task:exec:";
    private final MetricsService metricsService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "task-execution-topic", groupId = "loom-lanes-workers")
    public void handleTask(String message) throws Exception{
        try {
            TaskRequest task = objectMapper.readValue(message, TaskRequest.class);

            if ("FAIL_ME".equalsIgnoreCase(task.getType())) throw new RuntimeException("Simulated system failure for task: " + task.getTaskId());

            String lockKey = IDEMPOTENCY_KEY + task.getTaskId();
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(lockKey, "PROCESSING", Duration.ofMinutes(10));

            if (Boolean.FALSE.equals(isNew)){
                log.warn("Task {} is already being processed. Skipping.", task.getTaskId());
                return;
            }

            process(task);

            redisTemplate.opsForValue().set(lockKey, "COMPLETED", Duration.ofHours(1));
            metricsService.incrementSuccess();
            log.info("Task {} completed successfully.", task.getTaskId());
        }
        catch (Exception e){
            metricsService.incrementFailure();
            log.error("Error processing task: {}", e.getMessage());
            throw e;
        }
    }

    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
        log.error(" \uD83D\uDED1 DEAD LETTER QUEUE: Task permanently failed. Topic: {} | Message: {} ", topic, message);
    }

    private void process(TaskRequest task) throws InterruptedException{
        log.info("Executing Task: {} [Type: {}] on Thread: {}", task.getTaskId(), task.getType(), Thread.currentThread());

        Thread.sleep(2000);
    }
}
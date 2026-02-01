package com.loomlanes.engine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskPoller {
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String,String> kafkaTemplate;

    private static final String QUEUE_KEY = "tasks:priority_queue";
    private static final String TOPIC = "task-execution-topic";

    @Scheduled(fixedDelay = 1000)
    public void pollAndDispatch(){
        Set<ZSetOperations.TypedTuple<String>> tasks = redisTemplate.opsForZSet().popMin(QUEUE_KEY,10);

        if (tasks!=null && !tasks.isEmpty()){
            log.info("Polled {} tasks from Redis priority queue", tasks.size());

            tasks.forEach( tuple -> {
                String taskJson = tuple.getValue();
                kafkaTemplate.send(TOPIC,taskJson);
            });
        }
    }
}

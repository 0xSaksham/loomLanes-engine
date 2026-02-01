package com.loomlanes.engine.controller;

import com.loomlanes.engine.model.TaskRequest;
import com.loomlanes.engine.service.RateLimiterService;
import com.loomlanes.engine.service.TaskQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final RateLimiterService rateLimiter;
    private final TaskQueueService queueService;

    @PostMapping
    public ResponseEntity<String> submitTask(
            @Valid @RequestBody TaskRequest request,
            @RequestHeader(value = "X-Client-Id", defaultValue = "default-client") String clientId) {

        if (!rateLimiter.isAllowed(clientId, 10, 60)) {
            log.warn("Rate limit hit for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
        }

        queueService.enqueue(request);

        log.info("✅ Task {} accepted on thread: {}", request.getTaskId(), Thread.currentThread());

        return ResponseEntity.accepted().body("Task " + request.getTaskId() + " is in the priority lane.");
    }
}
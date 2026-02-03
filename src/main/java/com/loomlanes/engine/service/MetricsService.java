package com.loomlanes.engine.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
    private final Counter taskSuccessCounter;
    private final Counter taskFailureCounter;

    public MetricsService(MeterRegistry registry){
        this.taskSuccessCounter = Counter
                .builder("loomLanes_tasks_processed_total")
                .description("Total Tasks Successfully Processed")
                .tag("status", "success")
                .register(registry);

        this.taskFailureCounter = Counter
                .builder("loomLanes_tasks_processed_total")
                .description("Total Tasks failed")
                .tag("status", "failure")
                .register(registry);
    }

    public void incrementSuccess() { taskSuccessCounter.increment(); }
    public void incrementFailure() { taskFailureCounter.increment(); }
}

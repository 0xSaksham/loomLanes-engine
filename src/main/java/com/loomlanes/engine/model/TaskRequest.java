package com.loomlanes.engine.model;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "Task ID is mandatory")
    private String taskId;

    @NotBlank(message = "Task Type is mandatory")
    private String type;

    @NotNull
    @Min(1) @Max(30)
    private Integer priority;

    private Object payload;
}

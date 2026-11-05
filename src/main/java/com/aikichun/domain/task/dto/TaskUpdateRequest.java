package com.aikichun.domain.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    @Size(max = 50, message = "Task code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 500, message = "Link must not exceed 500 characters")
    private String link;

    private java.math.BigDecimal weight;

    private Boolean active;

    private java.util.List<Long> checkDefinitionIds;
}

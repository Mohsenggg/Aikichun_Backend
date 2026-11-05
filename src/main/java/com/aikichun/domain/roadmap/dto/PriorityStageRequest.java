package com.aikichun.domain.roadmap.dto;

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
public class PriorityStageRequest {

    @NotBlank(message = "Priority Stage name is required")
    @Size(max = 100, message = "Priority Stage name must not exceed 100 characters")
    private String name;

    @Size(max = 30, message = "Color code must not exceed 30 characters")
    private String color;

    private String description;
}

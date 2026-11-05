package com.aikichun.domain.roadmap.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageRequest {

    @NotNull(message = "Level ID is required")
    private Long levelId;

    private Long priorityStageId;

    @Size(max = 50, message = "Stage code must not exceed 50 characters")
    private String code;

    @Size(max = 150, message = "Stage name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Link must not exceed 500 characters")
    private String link;

    @DecimalMin(value = "0.01", message = "Stage weight must be greater than zero")
    @Builder.Default
    private BigDecimal weight = BigDecimal.valueOf(10.00);

    private Integer position;
}

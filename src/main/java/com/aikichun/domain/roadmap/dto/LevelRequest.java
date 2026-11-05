package com.aikichun.domain.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelRequest {

    @NotBlank(message = "Level name is required")
    @Size(max = 150, message = "Level name must not exceed 150 characters")
    private String name;

    private String description;

    private Integer position;

    @Size(max = 500, message = "Link must not exceed 500 characters")
    private String link;

    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Level weight must be greater than zero")
    @Builder.Default
    private java.math.BigDecimal weight = java.math.BigDecimal.valueOf(10.00);
}

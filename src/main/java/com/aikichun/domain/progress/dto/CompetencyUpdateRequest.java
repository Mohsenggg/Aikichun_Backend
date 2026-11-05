package com.aikichun.domain.progress.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyUpdateRequest {

    @NotNull(message = "knowledge checkbox state is required")
    private Boolean knowledge;

    @NotNull(message = "practice checkbox state is required")
    private Boolean practice;

    @NotNull(message = "pressure checkbox state is required")
    private Boolean pressure;

    @NotNull(message = "application checkbox state is required")
    private Boolean application;
}

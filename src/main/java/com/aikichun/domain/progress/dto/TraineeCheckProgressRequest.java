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
public class TraineeCheckProgressRequest {

    @NotNull(message = "Completed status is required")
    private Boolean completed;
}

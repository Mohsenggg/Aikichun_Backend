package com.aikichun.domain.roadmap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageTaskMoveRequest {

    @NotNull(message = "Target Stage ID is required")
    private Long targetStageId;

    private Integer targetPosition;
}

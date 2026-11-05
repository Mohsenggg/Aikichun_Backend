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
public class StageTaskReorderRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Position is required")
    private Integer position;
}

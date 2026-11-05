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
public class LevelReorderRequest {

    @NotNull(message = "Level ID is required")
    private Long id;

    @NotNull(message = "Position is required")
    private Integer position;
}

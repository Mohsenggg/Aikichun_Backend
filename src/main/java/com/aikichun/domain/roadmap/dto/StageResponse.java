package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.roadmap.entity.RoadmapStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponse {
    private Long id;
    private Long levelId;
    private PriorityStageResponse priorityStage;
    private String code;
    private String name;
    private String link;
    private BigDecimal weight;
    private int position;
    @Builder.Default
    private List<StageTaskResponse> tasks = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public static StageResponse fromEntity(RoadmapStage stage) {
        if (stage == null) return null;
        return StageResponse.builder()
                .id(stage.getId())
                .levelId(stage.getLevel() != null ? stage.getLevel().getId() : null)
                .priorityStage(PriorityStageResponse.fromEntity(stage.getPriorityStage()))
                .code(stage.getCode())
                .name(stage.getName())
                .link(stage.getLink())
                .weight(stage.getWeight())
                .position(stage.getPosition())
                .tasks(stage.getStageTasks() != null
                        ? stage.getStageTasks().stream().map(StageTaskResponse::fromEntity).toList()
                        : new ArrayList<>())
                .createdAt(stage.getCreatedAt())
                .updatedAt(stage.getUpdatedAt())
                .build();
    }
}

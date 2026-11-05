package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.task.dto.TaskResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageTaskResponse {
    private Long id;
    private Long stageId;
    private TaskResponse task;
    private int position;

    @JsonProperty("isMain")
    private boolean isMain;

    private Instant createdAt;
    private Instant updatedAt;

    @JsonProperty("isMain")
    public boolean isMain() {
        return isMain;
    }

    @JsonProperty("isMain")
    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    public static StageTaskResponse fromEntity(RoadmapStageTask stageTask) {
        if (stageTask == null) return null;
        return StageTaskResponse.builder()
                .id(stageTask.getId())
                .stageId(stageTask.getStage() != null ? stageTask.getStage().getId() : null)
                .task(TaskResponse.fromEntity(stageTask.getTask()))
                .position(stageTask.getPosition())
                .isMain(stageTask.isMain())
                .createdAt(stageTask.getCreatedAt())
                .updatedAt(stageTask.getUpdatedAt())
                .build();
    }

    public static StageTaskResponse fromEntity(RoadmapStageTask stageTask, java.util.List<CheckDefinitionResponse> checks) {
        if (stageTask == null) return null;
        return StageTaskResponse.builder()
                .id(stageTask.getId())
                .stageId(stageTask.getStage() != null ? stageTask.getStage().getId() : null)
                .task(TaskResponse.fromEntity(stageTask.getTask(), checks))
                .position(stageTask.getPosition())
                .isMain(stageTask.isMain())
                .createdAt(stageTask.getCreatedAt())
                .updatedAt(stageTask.getUpdatedAt())
                .build();
    }
}

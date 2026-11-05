package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.task.dto.TaskResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeStageTaskResponse {
    private Long id;
    private Long stageId;
    private TaskResponse task;
    private int position;

    @JsonProperty("isMain")
    private boolean isMain;

    private TraineeTaskProgressResponse progress;

    @JsonProperty("isMain")
    public boolean isMain() {
        return isMain;
    }

    @JsonProperty("isMain")
    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    public static TraineeStageTaskResponse fromEntity(RoadmapStageTask stageTask, TraineeTaskProgressResponse progress) {
        if (stageTask == null) return null;
        return TraineeStageTaskResponse.builder()
                .id(stageTask.getId())
                .stageId(stageTask.getStage() != null ? stageTask.getStage().getId() : null)
                .task(TaskResponse.fromEntity(stageTask.getTask()))
                .position(stageTask.getPosition())
                .isMain(stageTask.isMain())
                .progress(progress)
                .build();
    }
}

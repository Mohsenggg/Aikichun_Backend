package com.aikichun.domain.progress.dto;

import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.progress.entity.TraineeTaskCheckProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeCheckProgressResponse {
    private Long checkDefinitionId;
    private String name;
    private String description;
    private boolean completed;
    private Instant completedAt;

    public static TraineeCheckProgressResponse fromEntity(TraineeTaskCheckProgress entity) {
        if (entity == null) return null;
        CheckDefinition def = entity.getCheckDefinition();
        return TraineeCheckProgressResponse.builder()
                .checkDefinitionId(def != null ? def.getId() : null)
                .name(def != null ? def.getName() : null)
                .description(def != null ? def.getDescription() : null)
                .completed(entity.isCompleted())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public static TraineeCheckProgressResponse defaultForCheck(CheckDefinition def) {
        if (def == null) return null;
        return TraineeCheckProgressResponse.builder()
                .checkDefinitionId(def.getId())
                .name(def.getName())
                .description(def.getDescription())
                .completed(false)
                .completedAt(null)
                .build();
    }
}

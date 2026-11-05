package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.roadmap.entity.PriorityStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityStageResponse {
    private Long id;
    private String name;
    private String color;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static PriorityStageResponse fromEntity(PriorityStage entity) {
        if (entity == null) return null;
        return PriorityStageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

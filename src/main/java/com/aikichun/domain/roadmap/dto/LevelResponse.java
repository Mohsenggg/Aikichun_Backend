package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.roadmap.entity.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelResponse {
    private Long id;
    private Long roadmapId;
    private String name;
    private String description;
    private int position;
    private String link;
    private java.math.BigDecimal weight;
    @Builder.Default
    private List<StageResponse> stages = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public static LevelResponse fromEntity(Level level) {
        if (level == null) return null;
        return LevelResponse.builder()
                .id(level.getId())
                .roadmapId(level.getRoadmap() != null ? level.getRoadmap().getId() : null)
                .name(level.getName())
                .description(level.getDescription())
                .position(level.getPosition())
                .link(level.getLink())
                .weight(level.getWeight())
                .stages(level.getStages() != null
                        ? level.getStages().stream().map(StageResponse::fromEntity).toList()
                        : new ArrayList<>())
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .build();
    }
}

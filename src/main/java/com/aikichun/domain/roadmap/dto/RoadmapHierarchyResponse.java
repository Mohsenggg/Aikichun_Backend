package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.roadmap.entity.AccessMode;
import com.aikichun.domain.roadmap.entity.Roadmap;
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
public class RoadmapHierarchyResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
    @Builder.Default
    private AccessMode accessMode = AccessMode.OPEN;
    @Builder.Default
    private List<LevelResponse> levels = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public static RoadmapHierarchyResponse fromEntity(Roadmap roadmap) {
        if (roadmap == null) return null;
        return RoadmapHierarchyResponse.builder()
                .id(roadmap.getId())
                .name(roadmap.getName())
                .description(roadmap.getDescription())
                .isActive(roadmap.isActive())
                .accessMode(roadmap.getAccessMode() != null ? roadmap.getAccessMode() : AccessMode.OPEN)
                .levels(roadmap.getLevels() != null
                        ? roadmap.getLevels().stream().map(LevelResponse::fromEntity).toList()
                        : new ArrayList<>())
                .createdAt(roadmap.getCreatedAt())
                .updatedAt(roadmap.getUpdatedAt())
                .build();
    }
}

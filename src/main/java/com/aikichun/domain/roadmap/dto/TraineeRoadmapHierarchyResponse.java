package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.roadmap.entity.AccessMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRoadmapHierarchyResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
    @Builder.Default
    private AccessMode accessMode = AccessMode.OPEN;
    @Builder.Default
    private List<TraineeLevelResponse> levels = new ArrayList<>();
    private TraineeRoadmapProgressResponse progressSummary;
}

package com.aikichun.domain.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRoadmapProgressResponse {
    private UUID traineeId;
    private Long roadmapId;
    private BigDecimal overallProgressPercentage;
    private BigDecimal totalCurriculumWeight;
    private BigDecimal earnedWeightedPoints;
    private int totalTasksCount;
    private int completedTasksCount;
    private int inProgressTasksCount;
}

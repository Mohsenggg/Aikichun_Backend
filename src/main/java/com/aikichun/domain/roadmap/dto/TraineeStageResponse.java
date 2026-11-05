package com.aikichun.domain.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeStageResponse {
    private Long id;
    private Long levelId;
    private PriorityStageResponse priorityStage;
    private String code;
    private String name;
    private String link;
    private BigDecimal weight;
    private int position;
    private BigDecimal progressPercentage;
    private BigDecimal percentageOfLevel;
    @Builder.Default
    private List<TraineeStageTaskResponse> tasks = new ArrayList<>();
}

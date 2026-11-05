package com.aikichun.domain.roadmap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TraineeLevelResponse {
    private Long id;
    private Long roadmapId;
    private String name;
    private String description;
    private String link;
    private BigDecimal weight;
    private int position;
    private BigDecimal progressPercentage;
    private int stepsCount;

    @JsonProperty("isLocked")
    private boolean isLocked;

    @Builder.Default
    private List<TraineeStageResponse> stages = new ArrayList<>();

    @JsonProperty("isLocked")
    public boolean isLocked() {
        return isLocked;
    }

    @JsonProperty("isLocked")
    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
}

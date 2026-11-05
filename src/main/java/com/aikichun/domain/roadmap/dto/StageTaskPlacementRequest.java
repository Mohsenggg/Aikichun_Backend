package com.aikichun.domain.roadmap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageTaskPlacementRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    private Integer position;

    @JsonProperty("isMain")
    @Builder.Default
    private Boolean isMain = false;

    @JsonProperty("isMain")
    public Boolean getIsMain() {
        return isMain;
    }

    @JsonProperty("isMain")
    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }
}

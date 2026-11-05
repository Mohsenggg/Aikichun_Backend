package com.aikichun.domain.roadmap.dto;

import com.aikichun.domain.roadmap.entity.AccessMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapRequest {

    @NotBlank(message = "Roadmap name is required")
    @Size(max = 150, message = "Roadmap name must not exceed 150 characters")
    private String name;

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private AccessMode accessMode;
}

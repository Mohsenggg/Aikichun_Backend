package com.aikichun.domain.check.dto;

import com.aikichun.domain.check.entity.CheckDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDefinitionResponse {
    private Long id;
    private String name;
    private String description;
    private long assignedTaskCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static CheckDefinitionResponse fromEntity(CheckDefinition entity) {
        if (entity == null) return null;
        return CheckDefinitionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static CheckDefinitionResponse fromEntity(CheckDefinition entity, long assignedTaskCount) {
        if (entity == null) return null;
        return CheckDefinitionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .assignedTaskCount(assignedTaskCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

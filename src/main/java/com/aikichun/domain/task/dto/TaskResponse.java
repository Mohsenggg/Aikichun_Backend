package com.aikichun.domain.task.dto;

import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.task.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private String link;
    private BigDecimal weight;
    private boolean active;
    @Builder.Default
    private List<CheckDefinitionResponse> checks = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse fromEntity(Task task) {
        return fromEntity(task, Collections.emptyList());
    }

    public static TaskResponse fromEntity(Task task, List<CheckDefinitionResponse> checks) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .code(task.getCode())
                .title(task.getTitle())
                .description(task.getDescription())
                .link(task.getLink())
                .weight(task.getWeight())
                .active(task.isActive())
                .checks(checks != null ? checks : new ArrayList<>())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}

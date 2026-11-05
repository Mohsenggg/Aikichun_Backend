package com.aikichun.domain.roadmap.entity;

import com.aikichun.common.entity.BaseEntity;
import com.aikichun.domain.task.entity.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "roadmap_stage_tasks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_stage_task", columnNames = {"stage_id", "task_id"})
    },
    indexes = {
        @Index(name = "idx_stage_tasks_stage_id", columnList = "stage_id"),
        @Index(name = "idx_stage_tasks_task_id", columnList = "task_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStageTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private RoadmapStage stage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean isMain = false;
}

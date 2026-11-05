package com.aikichun.domain.progress.entity;

import com.aikichun.common.entity.BaseEntity;
import com.aikichun.domain.check.entity.CheckDefinition;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "trainee_task_check_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_trainee_task_check", columnNames = {"trainee_id", "task_id", "check_definition_id"})
    },
    indexes = {
        @Index(name = "idx_check_prog_trainee_id", columnList = "trainee_id"),
        @Index(name = "idx_check_prog_task_id", columnList = "task_id"),
        @Index(name = "idx_check_prog_check_id", columnList = "check_definition_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraineeTaskCheckProgress extends BaseEntity {

    @Column(name = "trainee_id", nullable = false)
    private UUID traineeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_definition_id", nullable = false)
    private CheckDefinition checkDefinition;

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;
}

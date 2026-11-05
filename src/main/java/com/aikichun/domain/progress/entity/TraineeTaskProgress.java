package com.aikichun.domain.progress.entity;

import com.aikichun.common.entity.BaseEntity;
import com.aikichun.domain.task.entity.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "trainee_task_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_trainee_task", columnNames = {"trainee_id", "task_id"})
    },
    indexes = {
        @Index(name = "idx_progress_trainee_id", columnList = "trainee_id"),
        @Index(name = "idx_progress_task_id", columnList = "task_id"),
        @Index(name = "idx_progress_trainee_status", columnList = "trainee_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraineeTaskProgress extends BaseEntity {

    @Column(name = "trainee_id", nullable = false)
    private UUID traineeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "knowledge", nullable = false)
    @Builder.Default
    private boolean knowledge = false;

    @Column(name = "practice", nullable = false)
    @Builder.Default
    private boolean practice = false;

    @Column(name = "pressure", nullable = false)
    @Builder.Default
    private boolean pressure = false;

    @Column(name = "application", nullable = false)
    @Builder.Default
    private boolean application = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TaskProgressStatus status = TaskProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}

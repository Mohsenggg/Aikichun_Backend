package com.aikichun.domain.progress.dto;

import com.aikichun.domain.progress.engine.DynamicProgressEngine;
import com.aikichun.domain.progress.entity.TaskProgressStatus;
import com.aikichun.domain.progress.entity.TraineeTaskProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTaskProgressResponse {

    private Long id;
    private UUID traineeId;
    private Long taskId;
    private Long stageId;
    private String taskTitle;
    private boolean knowledge;
    private boolean practice;
    private boolean pressure;
    private boolean application;
    private BigDecimal progressPercentage;
    private TaskProgressStatus status;
    private int completedChecksCount;
    private int totalChecksCount;
    @Builder.Default
    private List<TraineeCheckProgressResponse> checks = new ArrayList<>();
    private Instant startedAt;
    private Instant completedAt;
    private Instant updatedAt;

    public static TraineeTaskProgressResponse fromEntity(TraineeTaskProgress progress) {
        return fromEntity(progress, null);
    }

    public static TraineeTaskProgressResponse fromEntity(TraineeTaskProgress progress, Long stageId) {
        if (progress == null) {
            return null;
        }

        BigDecimal calculatedPercentage = DynamicProgressEngine.calculateTaskProgressPercentage(
                progress.isKnowledge(),
                progress.isPractice(),
                progress.isPressure(),
                progress.isApplication()
        );

        return TraineeTaskProgressResponse.builder()
                .id(progress.getId())
                .traineeId(progress.getTraineeId())
                .taskId(progress.getTask() != null ? progress.getTask().getId() : null)
                .stageId(stageId)
                .taskTitle(progress.getTask() != null ? progress.getTask().getTitle() : null)
                .knowledge(progress.isKnowledge())
                .practice(progress.isPractice())
                .pressure(progress.isPressure())
                .application(progress.isApplication())
                .progressPercentage(calculatedPercentage)
                .status(progress.getStatus())
                .checks(new ArrayList<>())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    public static TraineeTaskProgressResponse fromChecks(
            UUID traineeId,
            Long taskId,
            Long stageId,
            String taskTitle,
            List<TraineeCheckProgressResponse> checks,
            Instant startedAt,
            Instant completedAt) {

        int total = checks != null ? checks.size() : 0;
        int completed = (int) (checks != null ? checks.stream().filter(TraineeCheckProgressResponse::isCompleted).count() : 0);

        BigDecimal percentage = DynamicProgressEngine.calculateTaskProgressPercentage(completed, total);
        TaskProgressStatus status = DynamicProgressEngine.deriveStatus(completed, total);

        return TraineeTaskProgressResponse.builder()
                .id(null)
                .traineeId(traineeId)
                .taskId(taskId)
                .stageId(stageId)
                .taskTitle(taskTitle)
                .completedChecksCount(completed)
                .totalChecksCount(total)
                .progressPercentage(percentage)
                .status(status)
                .checks(checks != null ? checks : new ArrayList<>())
                .startedAt(startedAt)
                .completedAt(completedAt)
                .updatedAt(null)
                .build();
    }

    public static TraineeTaskProgressResponse defaultForTask(UUID traineeId, Long taskId, Long stageId, String taskTitle) {
        return TraineeTaskProgressResponse.builder()
                .id(null)
                .traineeId(traineeId)
                .taskId(taskId)
                .stageId(stageId)
                .taskTitle(taskTitle)
                .knowledge(false)
                .practice(false)
                .pressure(false)
                .application(false)
                .completedChecksCount(0)
                .totalChecksCount(0)
                .progressPercentage(BigDecimal.ZERO.setScale(2))
                .status(TaskProgressStatus.NOT_STARTED)
                .checks(new ArrayList<>())
                .startedAt(null)
                .completedAt(null)
                .updatedAt(null)
                .build();
    }
}

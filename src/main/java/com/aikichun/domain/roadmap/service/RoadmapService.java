package com.aikichun.domain.roadmap.service;

import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.engine.DynamicProgressEngine;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.domain.roadmap.dto.LevelResponse;
import com.aikichun.domain.roadmap.dto.PriorityStageResponse;
import com.aikichun.domain.roadmap.dto.RoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.dto.RoadmapRequest;
import com.aikichun.domain.roadmap.dto.StageResponse;
import com.aikichun.domain.roadmap.dto.StageTaskResponse;
import com.aikichun.domain.roadmap.dto.TraineeLevelResponse;
import com.aikichun.domain.roadmap.dto.TraineeRoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.dto.TraineeStageResponse;
import com.aikichun.domain.roadmap.dto.TraineeStageTaskResponse;
import com.aikichun.domain.roadmap.entity.AccessMode;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.Roadmap;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.RoadmapRepository;
import com.aikichun.domain.task.dto.TaskResponse;
import com.aikichun.domain.task.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final TraineeProgressService traineeProgressService;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @Transactional
    public Roadmap getOrCreateActiveRoadmap() {
        return roadmapRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> {
                    Roadmap defaultRoadmap = Roadmap.builder()
                            .name("Default Learning Roadmap")
                            .description("Core dynamic learning curriculum")
                            .isActive(true)
                            .accessMode(AccessMode.OPEN)
                            .build();
                    return roadmapRepository.save(defaultRoadmap);
                });
    }

    @Transactional
    public RoadmapHierarchyResponse getCoachRoadmapHierarchy(boolean includeInactive) {
        Roadmap roadmap = getOrCreateActiveRoadmap();

        // Batch-load all check assignments for tasks in this roadmap
        List<Long> allTaskIds = new ArrayList<>();
        if (roadmap.getLevels() != null) {
            for (Level level : roadmap.getLevels()) {
                if (level.getStages() != null) {
                    for (RoadmapStage stage : level.getStages()) {
                        if (stage.getStageTasks() != null) {
                            stage.getStageTasks().forEach(st -> {
                                if (st.getTask() != null) allTaskIds.add(st.getTask().getId());
                            });
                        }
                    }
                }
            }
        }
        Map<Long, List<CheckDefinitionResponse>> checksMap = allTaskIds.isEmpty()
                ? Map.of()
                : taskCheckAssignmentRepository.findByTaskIdInWithDefinition(allTaskIds).stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getTask().getId(),
                                Collectors.mapping(
                                        a -> CheckDefinitionResponse.fromEntity(a.getCheckDefinition()),
                                        Collectors.toList()
                                )
                        ));

        List<LevelResponse> levelResponses = new ArrayList<>();
        if (roadmap.getLevels() != null) {
            for (Level level : roadmap.getLevels()) {
                List<StageResponse> stageResponses = new ArrayList<>();
                if (level.getStages() != null) {
                    for (RoadmapStage stage : level.getStages()) {
                        List<StageTaskResponse> taskResponses = new ArrayList<>();
                        if (stage.getStageTasks() != null) {
                            for (RoadmapStageTask stageTask : stage.getStageTasks()) {
                                Task task = stageTask.getTask();
                                if (task != null && (includeInactive || task.isActive())) {
                                    List<CheckDefinitionResponse> checks =
                                            checksMap.getOrDefault(task.getId(), List.of());
                                    taskResponses.add(StageTaskResponse.fromEntity(stageTask, checks));
                                }
                            }
                        }

                        stageResponses.add(StageResponse.builder()
                                .id(stage.getId())
                                .levelId(level.getId())
                                .priorityStage(PriorityStageResponse.fromEntity(stage.getPriorityStage()))
                                .code(stage.getCode())
                                .name(stage.getName())
                                .link(stage.getLink())
                                .weight(stage.getWeight())
                                .position(stage.getPosition())
                                .tasks(taskResponses)
                                .createdAt(stage.getCreatedAt())
                                .updatedAt(stage.getUpdatedAt())
                                .build());
                    }
                }

                levelResponses.add(LevelResponse.builder()
                        .id(level.getId())
                        .roadmapId(roadmap.getId())
                        .name(level.getName())
                        .description(level.getDescription())
                        .link(level.getLink())
                        .position(level.getPosition())
                        .weight(level.getWeight())
                        .stages(stageResponses)
                        .createdAt(level.getCreatedAt())
                        .updatedAt(level.getUpdatedAt())
                        .build());
            }
        }

        return RoadmapHierarchyResponse.builder()
                .id(roadmap.getId())
                .name(roadmap.getName())
                .description(roadmap.getDescription())
                .isActive(roadmap.isActive())
                .accessMode(roadmap.getAccessMode() != null ? roadmap.getAccessMode() : AccessMode.OPEN)
                .levels(levelResponses)
                .createdAt(roadmap.getCreatedAt())
                .updatedAt(roadmap.getUpdatedAt())
                .build();
    }

    @Transactional
    public TraineeRoadmapHierarchyResponse getTraineeRoadmapHierarchy(UUID traineeId) {
        Roadmap roadmap = getOrCreateActiveRoadmap();

        // 1. Gather all active tasks in this roadmap
        List<Long> activeTaskIds = new ArrayList<>();
        if (roadmap.getLevels() != null) {
            for (Level level : roadmap.getLevels()) {
                if (level.getStages() != null) {
                    for (RoadmapStage stage : level.getStages()) {
                        if (stage.getStageTasks() != null) {
                            for (RoadmapStageTask stageTask : stage.getStageTasks()) {
                                if (stageTask.getTask() != null && stageTask.getTask().isActive()) {
                                    activeTaskIds.add(stageTask.getTask().getId());
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Batch load check assignments and existing trainee progress for active tasks
        Map<Long, List<CheckDefinitionResponse>> checksMap = activeTaskIds.isEmpty()
                ? Map.of()
                : taskCheckAssignmentRepository.findByTaskIdInWithDefinition(activeTaskIds).stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getTask().getId(),
                                Collectors.mapping(
                                        a -> CheckDefinitionResponse.fromEntity(a.getCheckDefinition()),
                                        Collectors.toList()
                                )
                        ));

        Map<Long, TraineeTaskProgressResponse> progressMap = traineeProgressService.getProgressMapForTrainee(traineeId, activeTaskIds);

        // 3. Assemble hierarchy with computed step, level, and task metrics
        List<TraineeLevelResponse> levelResponses = new ArrayList<>();
        if (roadmap.getLevels() != null) {
            for (Level level : roadmap.getLevels()) {
                // Compute total stage weight in level
                BigDecimal totalStageWeightInLevel = BigDecimal.ZERO;
                if (level.getStages() != null) {
                    for (RoadmapStage stage : level.getStages()) {
                        BigDecimal sw = stage.getWeight() != null ? stage.getWeight() : BigDecimal.valueOf(10.00);
                        totalStageWeightInLevel = totalStageWeightInLevel.add(sw);
                    }
                }

                BigDecimal earnedStagePointsInLevel = BigDecimal.ZERO;
                List<TraineeStageResponse> stageResponses = new ArrayList<>();

                if (level.getStages() != null) {
                    for (RoadmapStage stage : level.getStages()) {
                        BigDecimal stageWeight = stage.getWeight() != null ? stage.getWeight() : BigDecimal.valueOf(10.00);
                        BigDecimal percentageOfLevel = DynamicProgressEngine.calculatePercentage(stageWeight, totalStageWeightInLevel);

                        BigDecimal totalTaskWeightWithChecks = BigDecimal.ZERO;
                        BigDecimal earnedTaskPoints = BigDecimal.ZERO;
                        List<TraineeStageTaskResponse> taskResponses = new ArrayList<>();

                        if (stage.getStageTasks() != null) {
                            for (RoadmapStageTask stageTask : stage.getStageTasks()) {
                                Task task = stageTask.getTask();
                                if (task != null && task.isActive()) {
                                    List<CheckDefinitionResponse> taskChecks =
                                            checksMap.getOrDefault(task.getId(), List.of());

                                    TraineeTaskProgressResponse taskProgress = progressMap.get(task.getId());
                                    if (taskProgress == null) {
                                        taskProgress = TraineeTaskProgressResponse.defaultForTask(
                                                traineeId,
                                                task.getId(),
                                                stage.getId(),
                                                task.getTitle()
                                        );
                                    } else {
                                        taskProgress.setStageId(stage.getId());
                                    }

                                    int totalChecks = taskProgress.getTotalChecksCount();
                                    int completedChecks = taskProgress.getCompletedChecksCount();
                                    BigDecimal taskWeight = task.getWeight() != null ? task.getWeight() : BigDecimal.valueOf(10.00);

                                    // Zero-check tasks are excluded from the step denominator
                                    if (totalChecks > 0) {
                                        totalTaskWeightWithChecks = totalTaskWeightWithChecks.add(taskWeight);
                                        double taskFraction = (double) completedChecks / (double) totalChecks;
                                        earnedTaskPoints = earnedTaskPoints.add(taskWeight.multiply(BigDecimal.valueOf(taskFraction)));
                                    }

                                    taskResponses.add(TraineeStageTaskResponse.builder()
                                            .id(stageTask.getId())
                                            .stageId(stage.getId())
                                            .task(TaskResponse.fromEntity(task, taskChecks))
                                            .position(stageTask.getPosition())
                                            .isMain(stageTask.isMain())
                                            .progress(taskProgress)
                                            .build());
                                }
                            }
                        }

                        BigDecimal stageProgressPercentage = DynamicProgressEngine.calculatePercentage(earnedTaskPoints, totalTaskWeightWithChecks);
                        double stageFraction = stageProgressPercentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP).doubleValue();
                        earnedStagePointsInLevel = earnedStagePointsInLevel.add(stageWeight.multiply(BigDecimal.valueOf(stageFraction)));

                        stageResponses.add(TraineeStageResponse.builder()
                                .id(stage.getId())
                                .levelId(level.getId())
                                .priorityStage(PriorityStageResponse.fromEntity(stage.getPriorityStage()))
                                .code(stage.getCode())
                                .name(stage.getName())
                                .link(stage.getLink())
                                .weight(stage.getWeight())
                                .position(stage.getPosition())
                                .progressPercentage(stageProgressPercentage)
                                .percentageOfLevel(percentageOfLevel)
                                .tasks(taskResponses)
                                .build());
                    }
                }

                BigDecimal levelProgressPercentage = DynamicProgressEngine.calculatePercentage(earnedStagePointsInLevel, totalStageWeightInLevel);

                levelResponses.add(TraineeLevelResponse.builder()
                        .id(level.getId())
                        .roadmapId(roadmap.getId())
                        .name(level.getName())
                        .description(level.getDescription())
                        .link(level.getLink())
                        .position(level.getPosition())
                        .weight(level.getWeight())
                        .progressPercentage(levelProgressPercentage)
                        .stepsCount(stageResponses.size())
                        .stages(stageResponses)
                        .build());
            }
        }

        // 4. Resolve access control / locking
        AccessMode accessMode = roadmap.getAccessMode() != null ? roadmap.getAccessMode() : AccessMode.OPEN;
        if (accessMode == AccessMode.OPEN) {
            levelResponses.forEach(lvl -> lvl.setLocked(false));
        } else if (accessMode == AccessMode.PROGRESSIVE) {
            // Find the first level not 100% completed
            int unlockedMaxPosition = Integer.MAX_VALUE;
            for (TraineeLevelResponse lvl : levelResponses) {
                if (lvl.getProgressPercentage() == null || lvl.getProgressPercentage().compareTo(BigDecimal.valueOf(100.00)) < 0) {
                    unlockedMaxPosition = lvl.getPosition();
                    break;
                }
            }
            final int maxPos = unlockedMaxPosition;
            levelResponses.forEach(lvl -> lvl.setLocked(lvl.getPosition() > maxPos));
        }

        // 5. Calculate dynamic overall progress summary
        TraineeRoadmapProgressResponse progressSummary = traineeProgressService.calculateRoadmapProgress(traineeId, roadmap.getId());

        return TraineeRoadmapHierarchyResponse.builder()
                .id(roadmap.getId())
                .name(roadmap.getName())
                .description(roadmap.getDescription())
                .isActive(roadmap.isActive())
                .accessMode(accessMode)
                .levels(levelResponses)
                .progressSummary(progressSummary)
                .build();
    }

    @Transactional
    public RoadmapHierarchyResponse updateRoadmap(Long id, RoadmapRequest request) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + id));

        roadmap.setName(request.getName().trim());
        if (request.getDescription() != null) {
            roadmap.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            roadmap.setActive(request.getIsActive());
        }
        if (request.getAccessMode() != null) {
            roadmap.setAccessMode(request.getAccessMode());
        }

        Roadmap saved = roadmapRepository.save(roadmap);
        return getCoachRoadmapHierarchy(true);
    }
}

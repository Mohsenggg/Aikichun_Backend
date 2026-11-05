package com.aikichun.domain.progress.service;

import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.dto.CompetencyUpdateRequest;
import com.aikichun.domain.progress.dto.TraineeCheckProgressResponse;
import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.engine.DynamicProgressEngine;
import com.aikichun.domain.progress.entity.TaskProgressStatus;
import com.aikichun.domain.progress.entity.TraineeTaskCheckProgress;
import com.aikichun.domain.progress.entity.TraineeTaskProgress;
import com.aikichun.domain.progress.repository.TraineeTaskCheckProgressRepository;
import com.aikichun.domain.progress.repository.TraineeTaskProgressRepository;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.LevelRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageTaskRepository;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraineeProgressService {

    private final TraineeTaskProgressRepository progressRepository;
    private final TraineeTaskCheckProgressRepository traineeTaskCheckProgressRepository;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;
    private final CheckDefinitionRepository checkDefinitionRepository;
    private final RoadmapStageTaskRepository stageTaskRepository;
    private final RoadmapStageRepository stageRepository;
    private final LevelRepository levelRepository;
    private final TaskService taskService;

    @Transactional
    public TraineeTaskProgressResponse toggleCheckProgress(
            UUID traineeId,
            Long taskId,
            Long checkDefinitionId,
            boolean completed) {
        Task task = taskService.findTaskEntity(taskId);
        CheckDefinition checkDefinition = checkDefinitionRepository.findById(checkDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Check Definition not found with ID: " + checkDefinitionId));

        TraineeTaskCheckProgress checkProgress = traineeTaskCheckProgressRepository
                .findByTraineeIdAndTaskIdAndCheckDefinitionId(traineeId, taskId, checkDefinitionId)
                .orElseGet(() -> TraineeTaskCheckProgress.builder()
                        .traineeId(traineeId)
                        .task(task)
                        .checkDefinition(checkDefinition)
                        .build());

        checkProgress.setCompleted(completed);
        checkProgress.setCompletedAt(completed ? Instant.now() : null);
        traineeTaskCheckProgressRepository.save(checkProgress);

        return getTaskProgress(traineeId, taskId);
    }

    @Transactional
    public TraineeTaskProgressResponse updateCompetencyProgress(UUID traineeId, Long taskId, CompetencyUpdateRequest request) {
        Task task = taskService.findTaskEntity(taskId);

        TraineeTaskProgress progress = progressRepository.findByTraineeIdAndTaskId(traineeId, taskId)
                .orElseGet(() -> TraineeTaskProgress.builder()
                        .traineeId(traineeId)
                        .task(task)
                        .build());

        boolean k = Boolean.TRUE.equals(request.getKnowledge());
        boolean p = Boolean.TRUE.equals(request.getPractice());
        boolean pr = Boolean.TRUE.equals(request.getPressure());
        boolean a = Boolean.TRUE.equals(request.getApplication());

        progress.setKnowledge(k);
        progress.setPractice(p);
        progress.setPressure(pr);
        progress.setApplication(a);

        TaskProgressStatus derivedStatus = DynamicProgressEngine.deriveStatus(k, p, pr, a);
        progress.setStatus(derivedStatus);

        Instant now = Instant.now();
        if (derivedStatus != TaskProgressStatus.NOT_STARTED && progress.getStartedAt() == null) {
            progress.setStartedAt(now);
        }

        if (derivedStatus == TaskProgressStatus.COMPLETED) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(now);
            }
        } else {
            progress.setCompletedAt(null);
        }

        TraineeTaskProgress saved = progressRepository.save(progress);

        Long stageId = stageTaskRepository.findActiveStageTasksByTaskId(taskId).stream()
                .findFirst()
                .map(st -> st.getStage().getId())
                .orElse(null);

        return TraineeTaskProgressResponse.fromEntity(saved, stageId);
    }

    @Transactional(readOnly = true)
    public TraineeTaskProgressResponse getTaskProgress(UUID traineeId, Long taskId) {
        Task task = taskService.findTaskEntity(taskId);

        Long stageId = stageTaskRepository.findActiveStageTasksByTaskId(taskId).stream()
                .findFirst()
                .map(st -> st.getStage().getId())
                .orElse(null);

        List<TaskCheckAssignment> assignments = taskCheckAssignmentRepository.findByTaskIdWithDefinition(taskId);
        List<TraineeTaskCheckProgress> progressList = traineeTaskCheckProgressRepository.findByTraineeIdAndTaskId(traineeId, taskId);

        Map<Long, TraineeTaskCheckProgress> completedMap = progressList.stream()
                .collect(Collectors.toMap(p -> p.getCheckDefinition().getId(), p -> p, (a, b) -> a));

        List<TraineeCheckProgressResponse> checkResponses = assignments.stream()
                .map(a -> {
                    CheckDefinition def = a.getCheckDefinition();
                    TraineeTaskCheckProgress p = completedMap.get(def.getId());
                    boolean completed = p != null && p.isCompleted();
                    Instant completedAt = p != null ? p.getCompletedAt() : null;
                    return TraineeCheckProgressResponse.builder()
                            .checkDefinitionId(def.getId())
                            .name(def.getName())
                            .description(def.getDescription())
                            .completed(completed)
                            .completedAt(completedAt)
                            .build();
                })
                .toList();

        Instant startedAt = progressList.stream().map(TraineeTaskCheckProgress::getCompletedAt).filter(Objects::nonNull).min(Instant::compareTo).orElse(null);
        boolean allDone = !checkResponses.isEmpty() && checkResponses.stream().allMatch(TraineeCheckProgressResponse::isCompleted);
        Instant completedAt = allDone ? progressList.stream().map(TraineeTaskCheckProgress::getCompletedAt).filter(Objects::nonNull).max(Instant::compareTo).orElse(null) : null;

        return TraineeTaskProgressResponse.fromChecks(
                traineeId,
                task.getId(),
                stageId,
                task.getTitle(),
                checkResponses,
                startedAt,
                completedAt
        );
    }

    @Transactional(readOnly = true)
    public TraineeRoadmapProgressResponse calculateRoadmapProgress(UUID traineeId, Long roadmapId) {
        List<Level> activeLevels = levelRepository.findAllActiveLevelsWithHierarchyInRoadmap(roadmapId);

        if (activeLevels.isEmpty()) {
            return buildEmptyRoadmapProgress(traineeId, roadmapId);
        }

        stageRepository.findAllActiveStagesWithTasksInRoadmap(roadmapId);

        List<Long> allTaskIds = activeLevels.stream()
                .filter(l -> l.getStages() != null)
                .flatMap(l -> l.getStages().stream())
                .filter(s -> s.getStageTasks() != null)
                .flatMap(s -> s.getStageTasks().stream())
                .map(RoadmapStageTask::getTask)
                .filter(t -> t != null && t.isActive())
                .map(Task::getId)
                .distinct()
                .toList();

        if (allTaskIds.isEmpty()) {
            return buildEmptyRoadmapProgress(traineeId, roadmapId);
        }

        // 1. Load task check assignments and trainee check progresses
        List<TaskCheckAssignment> allAssignments = taskCheckAssignmentRepository.findByTaskIdInWithDefinition(allTaskIds);
        Map<Long, List<TaskCheckAssignment>> assignmentsByTask = allAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getTask().getId()));

        List<TraineeTaskCheckProgress> allCheckProgresses = traineeTaskCheckProgressRepository
                .findByTraineeIdAndTaskIdInWithDefinition(traineeId, allTaskIds);
        Map<String, TraineeTaskCheckProgress> progressMap = allCheckProgresses.stream()
                .collect(Collectors.toMap(
                        p -> p.getTask().getId() + "_" + p.getCheckDefinition().getId(),
                        p -> p,
                        (a, b) -> a
                ));

        int totalTasksCount = 0;
        int completedTasksCount = 0;
        int inProgressTasksCount = 0;

        BigDecimal totalRoadmapWeight = BigDecimal.ZERO;
        BigDecimal earnedRoadmapPoints = BigDecimal.ZERO;

        for (Level level : activeLevels) {
            BigDecimal levelWeight = level.getWeight() != null ? level.getWeight() : BigDecimal.valueOf(10.00);
            totalRoadmapWeight = totalRoadmapWeight.add(levelWeight);

            BigDecimal totalStepWeightInLevel = BigDecimal.ZERO;
            BigDecimal earnedStepPointsInLevel = BigDecimal.ZERO;

            if (level.getStages() != null) {
                for (RoadmapStage stage : level.getStages()) {
                    BigDecimal stageWeight = stage.getWeight() != null ? stage.getWeight() : BigDecimal.valueOf(10.00);
                    totalStepWeightInLevel = totalStepWeightInLevel.add(stageWeight);

                    BigDecimal totalTaskSubweightInStep = BigDecimal.ZERO;
                    BigDecimal earnedTaskSubweightInStep = BigDecimal.ZERO;

                    if (stage.getStageTasks() != null) {
                        for (RoadmapStageTask stageTask : stage.getStageTasks()) {
                            Task task = stageTask.getTask();
                            if (task == null || !task.isActive()) continue;

                            totalTasksCount++;
                            BigDecimal taskSubweight = task.getWeight() != null ? task.getWeight() : BigDecimal.valueOf(10.00);

                            List<TaskCheckAssignment> taskChecks = assignmentsByTask.getOrDefault(task.getId(), Collections.emptyList());
                            int totalChecks = taskChecks.size();
                            int completedChecks = 0;

                            for (TaskCheckAssignment assignment : taskChecks) {
                                TraineeTaskCheckProgress p = progressMap.get(task.getId() + "_" + assignment.getCheckDefinition().getId());
                                if (p != null && p.isCompleted()) {
                                    completedChecks++;
                                }
                            }

                            if (totalChecks > 0) {
                                totalTaskSubweightInStep = totalTaskSubweightInStep.add(taskSubweight);
                                double taskFraction = (double) completedChecks / (double) totalChecks;
                                earnedTaskSubweightInStep = earnedTaskSubweightInStep.add(taskSubweight.multiply(BigDecimal.valueOf(taskFraction)));

                                if (completedChecks == totalChecks) {
                                    completedTasksCount++;
                                } else if (completedChecks > 0) {
                                    inProgressTasksCount++;
                                }
                            }
                        }
                    }

                    // Step progress fraction (0.0 to 1.0)
                    double stepFraction = 0.0;
                    if (totalTaskSubweightInStep.compareTo(BigDecimal.ZERO) > 0) {
                        stepFraction = earnedTaskSubweightInStep.divide(totalTaskSubweightInStep, 6, RoundingMode.HALF_UP).doubleValue();
                    }
                    earnedStepPointsInLevel = earnedStepPointsInLevel.add(stageWeight.multiply(BigDecimal.valueOf(stepFraction)));
                }
            }

            // Level progress fraction (0.0 to 1.0)
            double levelFraction = 0.0;
            if (totalStepWeightInLevel.compareTo(BigDecimal.ZERO) > 0) {
                levelFraction = earnedStepPointsInLevel.divide(totalStepWeightInLevel, 6, RoundingMode.HALF_UP).doubleValue();
            }
            earnedRoadmapPoints = earnedRoadmapPoints.add(levelWeight.multiply(BigDecimal.valueOf(levelFraction)));
        }

        BigDecimal roadmapPercentage = BigDecimal.ZERO;
        if (totalRoadmapWeight.compareTo(BigDecimal.ZERO) > 0) {
            roadmapPercentage = earnedRoadmapPoints
                    .divide(totalRoadmapWeight, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return TraineeRoadmapProgressResponse.builder()
                .traineeId(traineeId)
                .roadmapId(roadmapId)
                .overallProgressPercentage(roadmapPercentage)
                .totalCurriculumWeight(totalRoadmapWeight.setScale(2, RoundingMode.HALF_UP))
                .earnedWeightedPoints(earnedRoadmapPoints.setScale(2, RoundingMode.HALF_UP))
                .totalTasksCount(totalTasksCount)
                .completedTasksCount(completedTasksCount)
                .inProgressTasksCount(inProgressTasksCount)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<Long, TraineeTaskProgressResponse> getProgressMapForTrainee(UUID traineeId, Collection<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TaskCheckAssignment> allAssignments = taskCheckAssignmentRepository.findByTaskIdInWithDefinition(taskIds);
        Map<Long, List<TaskCheckAssignment>> assignmentsByTask = allAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getTask().getId()));

        List<TraineeTaskCheckProgress> allProgress = traineeTaskCheckProgressRepository.findByTraineeIdAndTaskIdInWithDefinition(traineeId, taskIds);
        Map<String, TraineeTaskCheckProgress> progressByTaskAndCheck = allProgress.stream()
                .collect(Collectors.toMap(
                        p -> p.getTask().getId() + "_" + p.getCheckDefinition().getId(),
                        p -> p,
                        (a, b) -> a
                ));

        Map<Long, TraineeTaskProgressResponse> result = new HashMap<>();
        for (Long taskId : taskIds) {
            List<TaskCheckAssignment> assignments = assignmentsByTask.getOrDefault(taskId, Collections.emptyList());
            List<TraineeCheckProgressResponse> checkResponses = assignments.stream()
                    .map(a -> {
                        CheckDefinition def = a.getCheckDefinition();
                        TraineeTaskCheckProgress p = progressByTaskAndCheck.get(taskId + "_" + def.getId());
                        boolean completed = p != null && p.isCompleted();
                        Instant completedAt = p != null ? p.getCompletedAt() : null;
                        return TraineeCheckProgressResponse.builder()
                                .checkDefinitionId(def.getId())
                                .name(def.getName())
                                .description(def.getDescription())
                                .completed(completed)
                                .completedAt(completedAt)
                                .build();
                    })
                    .toList();

            result.put(taskId, TraineeTaskProgressResponse.fromChecks(
                    traineeId,
                    taskId,
                    null,
                    null,
                    checkResponses,
                    null,
                    null
            ));
        }

        return result;
    }

    private TraineeRoadmapProgressResponse buildEmptyRoadmapProgress(UUID traineeId, Long roadmapId) {
        return TraineeRoadmapProgressResponse.builder()
                .traineeId(traineeId)
                .roadmapId(roadmapId)
                .overallProgressPercentage(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .totalCurriculumWeight(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .earnedWeightedPoints(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .totalTasksCount(0)
                .completedTasksCount(0)
                .inProgressTasksCount(0)
                .build();
    }
}

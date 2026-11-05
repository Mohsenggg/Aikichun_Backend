package com.aikichun.domain.roadmap.service;

import com.aikichun.common.exception.ConflictException;
import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.roadmap.dto.StageTaskMoveRequest;
import com.aikichun.domain.roadmap.dto.StageTaskPlacementRequest;
import com.aikichun.domain.roadmap.dto.StageTaskReorderRequest;
import com.aikichun.domain.roadmap.dto.StageTaskResponse;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.RoadmapStageTaskRepository;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageTaskService {

    private final RoadmapStageTaskRepository stageTaskRepository;
    private final RoadmapStageService roadmapStageService;
    private final TaskService taskService;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @Transactional
    public StageTaskResponse placeTaskInStage(Long stageId, StageTaskPlacementRequest request) {
        RoadmapStage stage = roadmapStageService.findStageEntity(stageId);
        Task task = taskService.findTaskEntity(request.getTaskId());

        if (stageTaskRepository.existsByStageIdAndTaskId(stageId, task.getId())) {
            throw new ConflictException("Task with ID " + task.getId() + " is already placed in Stage " + stageId);
        }

        int position = (request.getPosition() != null && request.getPosition() > 0)
                ? request.getPosition()
                : stageTaskRepository.findMaxPositionByStageId(stageId) + 1;

        boolean isMain = Boolean.TRUE.equals(request.getIsMain());
        if (isMain) {
            stageTaskRepository.resetMainTaskInStage(stageId);
        }

        RoadmapStageTask stageTask = RoadmapStageTask.builder()
                .stage(stage)
                .task(task)
                .position(position)
                .isMain(isMain)
                .build();

        RoadmapStageTask saved = stageTaskRepository.save(stageTask);
        List<CheckDefinitionResponse> checks = taskService.getCheckResponsesForTask(task.getId());
        return StageTaskResponse.fromEntity(saved, checks);
    }

    @Transactional
    public void removeTaskFromStage(Long stageId, Long taskId) {
        RoadmapStageTask stageTask = stageTaskRepository.findByStageIdAndTaskId(stageId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " is not placed in Stage " + stageId));

        stageTaskRepository.delete(stageTask);
    }

    @Transactional
    public List<StageTaskResponse> reorderTasksInStage(Long stageId, List<StageTaskReorderRequest> requests) {
        roadmapStageService.findStageEntity(stageId); // validate stage exists

        for (StageTaskReorderRequest req : requests) {
            RoadmapStageTask stageTask = stageTaskRepository.findByStageIdAndTaskId(stageId, req.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task " + req.getTaskId() + " not in Stage " + stageId));
            stageTask.setPosition(req.getPosition());
            stageTaskRepository.save(stageTask);
        }

        List<RoadmapStageTask> list = stageTaskRepository.findByStageIdOrderByPositionAsc(stageId);
        if (list.isEmpty()) return List.of();

        List<Long> taskIds = list.stream().map(st -> st.getTask().getId()).toList();
        List<TaskCheckAssignment> assignments = taskCheckAssignmentRepository.findByTaskIdInWithDefinition(taskIds);
        Map<Long, List<CheckDefinitionResponse>> map = assignments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTask().getId(),
                        Collectors.mapping(a -> CheckDefinitionResponse.fromEntity(a.getCheckDefinition()), Collectors.toList())
                ));

        return list.stream()
                .map(st -> StageTaskResponse.fromEntity(st, map.getOrDefault(st.getTask().getId(), List.of())))
                .toList();
    }

    @Transactional
    public StageTaskResponse moveTaskToStage(Long sourceStageId, Long taskId, StageTaskMoveRequest request) {
        RoadmapStageTask stageTask = stageTaskRepository.findByStageIdAndTaskId(sourceStageId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found in source Stage " + sourceStageId));

        Long targetStageId = request.getTargetStageId();
        RoadmapStage targetStage = roadmapStageService.findStageEntity(targetStageId);

        if (!sourceStageId.equals(targetStageId) && stageTaskRepository.existsByStageIdAndTaskId(targetStageId, taskId)) {
            throw new ConflictException("Task " + taskId + " is already present in target Stage " + targetStageId);
        }

        int targetPosition = (request.getTargetPosition() != null && request.getTargetPosition() > 0)
                ? request.getTargetPosition()
                : stageTaskRepository.findMaxPositionByStageId(targetStageId) + 1;

        stageTask.setStage(targetStage);
        stageTask.setPosition(targetPosition);
        stageTask.setMain(false); // Reset main status when moved across stages

        RoadmapStageTask updated = stageTaskRepository.save(stageTask);
        List<CheckDefinitionResponse> checks = taskService.getCheckResponsesForTask(taskId);
        return StageTaskResponse.fromEntity(updated, checks);
    }

    @Transactional
    public StageTaskResponse setMainTask(Long stageId, Long taskId) {
        RoadmapStageTask stageTask = stageTaskRepository.findByStageIdAndTaskId(stageId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + taskId + " not found in Stage " + stageId));

        stageTaskRepository.resetMainTaskInStage(stageId);
        stageTask.setMain(true);
        RoadmapStageTask updated = stageTaskRepository.save(stageTask);
        List<CheckDefinitionResponse> checks = taskService.getCheckResponsesForTask(taskId);
        return StageTaskResponse.fromEntity(updated, checks);
    }

    @Transactional
    public void removeMainTask(Long stageId) {
        roadmapStageService.findStageEntity(stageId); // validate stage exists
        stageTaskRepository.resetMainTaskInStage(stageId);
    }
}

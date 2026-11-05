package com.aikichun.domain.task.service;

import com.aikichun.common.exception.ConflictException;
import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.repository.TraineeTaskProgressRepository;
import com.aikichun.domain.task.dto.TaskCreateRequest;
import com.aikichun.domain.task.dto.TaskResponse;
import com.aikichun.domain.task.dto.TaskUpdateRequest;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TraineeTaskProgressRepository traineeTaskProgressRepository;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;
    private final CheckDefinitionRepository checkDefinitionRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        Task task = Task.builder()
                .code(request.getCode() != null ? request.getCode().trim() : null)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .link(request.getLink() != null ? request.getLink().trim() : null)
                .weight(request.getWeight() != null ? request.getWeight() : java.math.BigDecimal.valueOf(10.00))
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Task savedTask = taskRepository.save(task);

        if (request.getCheckDefinitionIds() != null) {
            syncCheckAssignments(savedTask, request.getCheckDefinitionIds());
        }

        List<CheckDefinitionResponse> checkResponses = getCheckResponsesForTask(savedTask.getId());
        return TaskResponse.fromEntity(savedTask, checkResponses);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = findTaskEntity(id);

        if (request.getCode() != null) {
            task.setCode(request.getCode().trim().isEmpty() ? null : request.getCode().trim());
        }
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getLink() != null) {
            task.setLink(request.getLink().trim().isEmpty() ? null : request.getLink().trim());
        }
        if (request.getWeight() != null) {
            task.setWeight(request.getWeight());
        }
        if (request.getActive() != null) {
            task.setActive(request.getActive());
        }

        Task updatedTask = taskRepository.save(task);

        if (request.getCheckDefinitionIds() != null) {
            syncCheckAssignments(updatedTask, request.getCheckDefinitionIds());
        }

        List<CheckDefinitionResponse> checkResponses = getCheckResponsesForTask(updatedTask.getId());
        return TaskResponse.fromEntity(updatedTask, checkResponses);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskEntity(id);
        List<CheckDefinitionResponse> checkResponses = getCheckResponsesForTask(id);
        return TaskResponse.fromEntity(task, checkResponses);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(boolean includeInactive) {
        return getAllTasks(includeInactive, null);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(boolean includeInactive, String search) {
        List<Task> tasks = includeInactive ? taskRepository.findAll() : taskRepository.findByActiveTrue();
        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase();
            tasks = tasks.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(term))
                            || (t.getDescription() != null && t.getDescription().toLowerCase().contains(term)))
                    .toList();
        }
        if (tasks.isEmpty()) return java.util.Collections.emptyList();

        List<Long> taskIds = tasks.stream().map(Task::getId).toList();
        List<TaskCheckAssignment> assignments = taskCheckAssignmentRepository.findByTaskIdInWithDefinition(taskIds);
        java.util.Map<Long, List<CheckDefinitionResponse>> map = assignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getTask().getId(),
                        java.util.stream.Collectors.mapping(a -> CheckDefinitionResponse.fromEntity(a.getCheckDefinition()), java.util.stream.Collectors.toList())
                ));

        return tasks.stream()
                .map(t -> TaskResponse.fromEntity(t, map.getOrDefault(t.getId(), java.util.Collections.emptyList())))
                .toList();
    }

    @Transactional
    public TaskResponse deactivateTask(Long id) {
        Task task = findTaskEntity(id);
        task.setActive(false);
        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask, getCheckResponsesForTask(id));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskEntity(id);

        if (traineeTaskProgressRepository.existsByTaskId(id)) {
            throw new ConflictException("Cannot delete task with existing trainee progress. Deactivate the task instead.");
        }

        taskCheckAssignmentRepository.deleteByTaskId(id);
        taskRepository.delete(task);
    }

    public Task findTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
    }

    public List<CheckDefinitionResponse> getCheckResponsesForTask(Long taskId) {
        return taskCheckAssignmentRepository.findByTaskIdWithDefinition(taskId).stream()
                .map(a -> CheckDefinitionResponse.fromEntity(a.getCheckDefinition()))
                .toList();
    }

    private void syncCheckAssignments(Task task, List<Long> checkDefinitionIds) {
        taskCheckAssignmentRepository.deleteByTaskId(task.getId());
        if (checkDefinitionIds == null || checkDefinitionIds.isEmpty()) return;

        List<Long> distinctIds = checkDefinitionIds.stream().distinct().toList();
        for (Long checkDefId : distinctIds) {
            CheckDefinition def = checkDefinitionRepository.findById(checkDefId)
                    .orElseThrow(() -> new ResourceNotFoundException("Check Definition not found with ID: " + checkDefId));

            TaskCheckAssignment assignment = TaskCheckAssignment.builder()
                    .task(task)
                    .checkDefinition(def)
                    .build();

            taskCheckAssignmentRepository.save(assignment);
        }
    }
}

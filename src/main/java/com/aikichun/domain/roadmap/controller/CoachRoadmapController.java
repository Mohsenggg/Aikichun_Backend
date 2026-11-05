package com.aikichun.domain.roadmap.controller;

import com.aikichun.domain.roadmap.dto.LevelReorderRequest;
import com.aikichun.domain.roadmap.dto.LevelRequest;
import com.aikichun.domain.roadmap.dto.LevelResponse;
import com.aikichun.domain.roadmap.dto.PriorityStageRequest;
import com.aikichun.domain.roadmap.dto.PriorityStageResponse;
import com.aikichun.domain.roadmap.dto.RoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.dto.RoadmapRequest;
import com.aikichun.domain.roadmap.dto.StageReorderRequest;
import com.aikichun.domain.roadmap.dto.StageRequest;
import com.aikichun.domain.roadmap.dto.StageResponse;
import com.aikichun.domain.roadmap.dto.StageWeightRequest;
import com.aikichun.domain.roadmap.dto.StageTaskMoveRequest;
import com.aikichun.domain.roadmap.dto.StageTaskPlacementRequest;
import com.aikichun.domain.roadmap.dto.StageTaskReorderRequest;
import com.aikichun.domain.roadmap.dto.StageTaskResponse;
import com.aikichun.domain.roadmap.service.LevelService;
import com.aikichun.domain.roadmap.service.PriorityStageService;
import com.aikichun.domain.roadmap.service.RoadmapService;
import com.aikichun.domain.roadmap.service.RoadmapStageService;
import com.aikichun.domain.roadmap.service.StageTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
public class CoachRoadmapController {

    private final RoadmapService roadmapService;
    private final LevelService levelService;
    private final PriorityStageService priorityStageService;
    private final RoadmapStageService roadmapStageService;
    private final StageTaskService stageTaskService;

    // --- Full Roadmap ---

    @GetMapping("/roadmap")
    public ResponseEntity<RoadmapHierarchyResponse> getCoachRoadmap(
            @RequestParam(defaultValue = "true") boolean includeInactive) {
        RoadmapHierarchyResponse response = roadmapService.getCoachRoadmapHierarchy(includeInactive);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/roadmap/{id}")
    public ResponseEntity<RoadmapHierarchyResponse> updateRoadmap(
            @PathVariable Long id,
            @Valid @RequestBody RoadmapRequest request) {
        RoadmapHierarchyResponse response = roadmapService.updateRoadmap(id, request);
        return ResponseEntity.ok(response);
    }

    // --- Levels ---

    @PostMapping("/levels")
    public ResponseEntity<LevelResponse> createLevel(@Valid @RequestBody LevelRequest request) {
        LevelResponse response = levelService.createLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/levels/{id}")
    public ResponseEntity<LevelResponse> updateLevel(
            @PathVariable Long id,
            @Valid @RequestBody LevelRequest request) {
        LevelResponse response = levelService.updateLevel(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        levelService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/levels/{id}/weight")
    public ResponseEntity<LevelResponse> updateLevelWeight(
            @PathVariable Long id,
            @RequestParam BigDecimal weight) {
        LevelResponse response = levelService.updateLevelWeight(id, weight);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/levels/reorder")
    public ResponseEntity<List<LevelResponse>> reorderLevels(
            @Valid @RequestBody List<LevelReorderRequest> requests) {
        List<LevelResponse> responses = levelService.reorderLevels(requests);
        return ResponseEntity.ok(responses);
    }

    // --- Priority Stages (Reusable Classification Labels) ---

    @GetMapping("/priority-stages")
    public ResponseEntity<List<PriorityStageResponse>> getAllPriorityStages() {
        List<PriorityStageResponse> responses = priorityStageService.getAllPriorityStages();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/priority-stages")
    public ResponseEntity<PriorityStageResponse> createPriorityStage(
            @Valid @RequestBody PriorityStageRequest request) {
        PriorityStageResponse response = priorityStageService.createPriorityStage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/priority-stages/{id}")
    public ResponseEntity<PriorityStageResponse> updatePriorityStage(
            @PathVariable Long id,
            @Valid @RequestBody PriorityStageRequest request) {
        PriorityStageResponse response = priorityStageService.updatePriorityStage(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/priority-stages/{id}")
    public ResponseEntity<Void> deletePriorityStage(@PathVariable Long id) {
        priorityStageService.deletePriorityStage(id);
        return ResponseEntity.noContent().build();
    }

    // --- Stages ---

    @PostMapping("/stages")
    public ResponseEntity<StageResponse> createStage(@Valid @RequestBody StageRequest request) {
        StageResponse response = roadmapStageService.createStage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<StageResponse> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody StageRequest request) {
        StageResponse response = roadmapStageService.updateStage(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/stages/{id}/weight")
    public ResponseEntity<StageResponse> updateStageWeight(
            @PathVariable Long id,
            @Valid @RequestBody StageWeightRequest request) {
        StageResponse response = roadmapStageService.updateStageWeight(id, request.getWeight());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable Long id) {
        roadmapStageService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stages/reorder")
    public ResponseEntity<List<StageResponse>> reorderStages(
            @Valid @RequestBody List<StageReorderRequest> requests) {
        List<StageResponse> responses = roadmapStageService.reorderStages(requests);
        return ResponseEntity.ok(responses);
    }

    // --- Stage Task Management & Main Task ---

    @PostMapping("/stages/{stageId}/tasks")
    public ResponseEntity<StageTaskResponse> placeTaskInStage(
            @PathVariable Long stageId,
            @Valid @RequestBody StageTaskPlacementRequest request) {
        StageTaskResponse response = stageTaskService.placeTaskInStage(stageId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<Void> removeTaskFromStage(
            @PathVariable Long stageId,
            @PathVariable Long taskId) {
        stageTaskService.removeTaskFromStage(stageId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stages/{stageId}/tasks/reorder")
    public ResponseEntity<List<StageTaskResponse>> reorderTasksInStage(
            @PathVariable Long stageId,
            @Valid @RequestBody List<StageTaskReorderRequest> requests) {
        List<StageTaskResponse> responses = stageTaskService.reorderTasksInStage(stageId, requests);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/stages/{stageId}/tasks/{taskId}/move")
    public ResponseEntity<StageTaskResponse> moveTaskToStage(
            @PathVariable Long stageId,
            @PathVariable Long taskId,
            @Valid @RequestBody StageTaskMoveRequest request) {
        StageTaskResponse response = stageTaskService.moveTaskToStage(stageId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/stages/{stageId}/tasks/{taskId}/main")
    public ResponseEntity<StageTaskResponse> setMainTask(
            @PathVariable Long stageId,
            @PathVariable Long taskId) {
        StageTaskResponse response = stageTaskService.setMainTask(stageId, taskId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/stages/{stageId}/main-task")
    public ResponseEntity<Void> removeMainTask(@PathVariable Long stageId) {
        stageTaskService.removeMainTask(stageId);
        return ResponseEntity.noContent().build();
    }
}

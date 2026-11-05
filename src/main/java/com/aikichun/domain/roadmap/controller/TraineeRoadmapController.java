package com.aikichun.domain.roadmap.controller;

import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.domain.roadmap.dto.TraineeRoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainee/{traineeId}/roadmap")
@RequiredArgsConstructor
public class TraineeRoadmapController {

    private final RoadmapService roadmapService;
    private final TraineeProgressService traineeProgressService;

    @GetMapping
    public ResponseEntity<TraineeRoadmapHierarchyResponse> getTraineeRoadmap(
            @PathVariable UUID traineeId) {
        TraineeRoadmapHierarchyResponse response = roadmapService.getTraineeRoadmapHierarchy(traineeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<TraineeRoadmapProgressResponse> getTraineeRoadmapProgress(
            @PathVariable UUID traineeId) {
        Long roadmapId = roadmapService.getOrCreateActiveRoadmap().getId();
        TraineeRoadmapProgressResponse response = traineeProgressService.calculateRoadmapProgress(traineeId, roadmapId);
        return ResponseEntity.ok(response);
    }
}

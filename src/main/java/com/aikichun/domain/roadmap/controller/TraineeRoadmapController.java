package com.aikichun.domain.roadmap.controller;

import com.aikichun.Security.UserPrincipal;
import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.domain.roadmap.dto.TraineeRoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.service.RoadmapService;
import com.aikichun.user.entites.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID traineeId) {
        assertAccess(principal, traineeId);
        TraineeRoadmapHierarchyResponse response = roadmapService.getTraineeRoadmapHierarchy(traineeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<TraineeRoadmapProgressResponse> getTraineeRoadmapProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID traineeId) {
        assertAccess(principal, traineeId);
        Long roadmapId = roadmapService.getOrCreateActiveRoadmap().getId();
        TraineeRoadmapProgressResponse response = traineeProgressService.calculateRoadmapProgress(traineeId, roadmapId);
        return ResponseEntity.ok(response);
    }

    private void assertAccess(UserPrincipal principal, UUID traineeId) {
        if (principal == null) {
            return;
        }
        if (principal.getRole() != Role.ADMIN && !principal.getId().equals(traineeId)) {
            throw new AccessDeniedException("Access denied: You cannot view or modify another trainee's data.");
        }
    }
}

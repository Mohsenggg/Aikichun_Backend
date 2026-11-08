package com.aikichun.domain.progress.controller;

import com.aikichun.Security.UserPrincipal;
import com.aikichun.common.exception.BusinessRuleException;
import com.aikichun.domain.progress.dto.CompetencyUpdateRequest;
import com.aikichun.domain.progress.dto.TraineeCheckProgressRequest;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.user.entites.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainee/{traineeId}/tasks/{taskId}")
@RequiredArgsConstructor
public class TraineeProgressController {

    private final TraineeProgressService traineeProgressService;

    @GetMapping("/progress")
    public ResponseEntity<TraineeTaskProgressResponse> getTaskProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID traineeId,
            @PathVariable Long taskId) {
        assertAccess(principal, traineeId, false);
        TraineeTaskProgressResponse response = traineeProgressService.getTaskProgress(traineeId, taskId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/competency")
    public ResponseEntity<TraineeTaskProgressResponse> updateCompetencyProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID traineeId,
            @PathVariable Long taskId,
            @Valid @RequestBody CompetencyUpdateRequest request) {
        assertAccess(principal, traineeId, true);
        TraineeTaskProgressResponse response = traineeProgressService.updateCompetencyProgress(traineeId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/checks/{checkDefinitionId}")
    public ResponseEntity<TraineeTaskProgressResponse> toggleCheckProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID traineeId,
            @PathVariable Long taskId,
            @PathVariable Long checkDefinitionId,
            @Valid @RequestBody TraineeCheckProgressRequest request) {
        assertAccess(principal, traineeId, true);
        TraineeTaskProgressResponse response = traineeProgressService.toggleCheckProgress(
                traineeId, taskId, checkDefinitionId, request.getCompleted());
        return ResponseEntity.ok(response);
    }

    private void assertAccess(UserPrincipal principal, UUID traineeId, boolean isModification) {
        if (principal == null) {
            return;
        }
        if (principal.getRole() != Role.ADMIN && !principal.getId().equals(traineeId)) {
            throw new AccessDeniedException("Access denied: You cannot view or modify another trainee's data.");
        }
        if (isModification && principal.getRole() != Role.ADMIN) {
            if (!principal.isSubscriptionActive(LocalDate.now())) {
                throw new BusinessRuleException("Subscription is inactive or expired. Please renew to record progress.");
            }
        }
    }
}

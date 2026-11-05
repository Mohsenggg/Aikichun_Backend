package com.aikichun.domain.progress.controller;

import com.aikichun.domain.progress.dto.CompetencyUpdateRequest;
import com.aikichun.domain.progress.dto.TraineeCheckProgressRequest;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.service.TraineeProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainee/{traineeId}/tasks/{taskId}")
@RequiredArgsConstructor
public class TraineeProgressController {

    private final TraineeProgressService traineeProgressService;

    @GetMapping("/progress")
    public ResponseEntity<TraineeTaskProgressResponse> getTaskProgress(
            @PathVariable UUID traineeId,
            @PathVariable Long taskId) {
        TraineeTaskProgressResponse response = traineeProgressService.getTaskProgress(traineeId, taskId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/competency")
    public ResponseEntity<TraineeTaskProgressResponse> updateCompetencyProgress(
            @PathVariable UUID traineeId,
            @PathVariable Long taskId,
            @Valid @RequestBody CompetencyUpdateRequest request) {
        TraineeTaskProgressResponse response = traineeProgressService.updateCompetencyProgress(traineeId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/checks/{checkDefinitionId}")
    public ResponseEntity<TraineeTaskProgressResponse> toggleCheckProgress(
            @PathVariable UUID traineeId,
            @PathVariable Long taskId,
            @PathVariable Long checkDefinitionId,
            @Valid @RequestBody TraineeCheckProgressRequest request) {
        TraineeTaskProgressResponse response = traineeProgressService.toggleCheckProgress(
                traineeId, taskId, checkDefinitionId, request.getCompleted());
        return ResponseEntity.ok(response);
    }
}

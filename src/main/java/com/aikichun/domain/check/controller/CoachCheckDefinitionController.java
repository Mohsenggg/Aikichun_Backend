package com.aikichun.domain.check.controller;

import com.aikichun.domain.check.dto.CheckDefinitionRequest;
import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.service.CheckDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach/check-definitions")
@RequiredArgsConstructor
public class CoachCheckDefinitionController {

    private final CheckDefinitionService checkDefinitionService;

    @GetMapping
    public ResponseEntity<List<CheckDefinitionResponse>> getAllCheckDefinitions() {
        List<CheckDefinitionResponse> responses = checkDefinitionService.getAllCheckDefinitions();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<CheckDefinitionResponse> createCheckDefinition(
            @Valid @RequestBody CheckDefinitionRequest request) {
        CheckDefinitionResponse response = checkDefinitionService.createCheckDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheckDefinitionResponse> updateCheckDefinition(
            @PathVariable Long id,
            @Valid @RequestBody CheckDefinitionRequest request) {
        CheckDefinitionResponse response = checkDefinitionService.updateCheckDefinition(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckDefinition(@PathVariable Long id) {
        checkDefinitionService.deleteCheckDefinition(id);
        return ResponseEntity.noContent().build();
    }
}

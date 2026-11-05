package com.aikichun.domain.roadmap.service;

import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.roadmap.dto.PriorityStageRequest;
import com.aikichun.domain.roadmap.dto.PriorityStageResponse;
import com.aikichun.domain.roadmap.entity.PriorityStage;
import com.aikichun.domain.roadmap.repository.PriorityStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorityStageService {

    private final PriorityStageRepository priorityStageRepository;

    @Transactional
    public PriorityStageResponse createPriorityStage(PriorityStageRequest request) {
        PriorityStage entity = PriorityStage.builder()
                .name(request.getName().trim())
                .color(request.getColor() != null ? request.getColor().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        PriorityStage saved = priorityStageRepository.save(entity);
        return PriorityStageResponse.fromEntity(saved);
    }

    @Transactional
    public PriorityStageResponse updatePriorityStage(Long id, PriorityStageRequest request) {
        PriorityStage entity = findPriorityStageEntity(id);
        entity.setName(request.getName().trim());
        if (request.getColor() != null) {
            entity.setColor(request.getColor().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        } else {
            entity.setDescription(null);
        }
        PriorityStage updated = priorityStageRepository.save(entity);
        return PriorityStageResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<PriorityStageResponse> getAllPriorityStages() {
        return priorityStageRepository.findAllByOrderByNameAsc().stream()
                .map(PriorityStageResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deletePriorityStage(Long id) {
        PriorityStage entity = findPriorityStageEntity(id);
        priorityStageRepository.delete(entity);
    }

    public PriorityStage findPriorityStageEntity(Long id) {
        return priorityStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Priority Stage not found with ID: " + id));
    }
}

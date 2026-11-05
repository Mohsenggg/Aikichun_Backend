package com.aikichun.domain.roadmap.service;

import com.aikichun.common.exception.ConflictException;
import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.roadmap.dto.StageReorderRequest;
import com.aikichun.domain.roadmap.dto.StageRequest;
import com.aikichun.domain.roadmap.dto.StageResponse;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.PriorityStage;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.repository.RoadmapStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapStageService {

    private final RoadmapStageRepository stageRepository;
    private final LevelService levelService;
    private final PriorityStageService priorityStageService;

    @Transactional
    public StageResponse createStage(StageRequest request) {
        Level level = levelService.findLevelEntity(request.getLevelId());
        Long roadmapId = level.getRoadmap() != null ? level.getRoadmap().getId() : null;

        String code = null;
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            code = request.getCode().trim();
            if (roadmapId != null && stageRepository.existsByRoadmapIdAndCodeIgnoreCase(roadmapId, code, null)) {
                throw new ConflictException("Stage code '" + code + "' is already in use in this roadmap.");
            }
        }

        PriorityStage priorityStage = null;
        if (request.getPriorityStageId() != null) {
            priorityStage = priorityStageService.findPriorityStageEntity(request.getPriorityStageId());
        }

        int position = (request.getPosition() != null && request.getPosition() > 0)
                ? request.getPosition()
                : stageRepository.findMaxPositionByLevelId(level.getId()) + 1;

        BigDecimal weight = request.getWeight() != null ? request.getWeight() : BigDecimal.valueOf(10.00);

        String name = request.getName() != null ? request.getName().trim() : "";

        RoadmapStage stage = RoadmapStage.builder()
                .level(level)
                .priorityStage(priorityStage)
                .code(code)
                .name(name)
                .link(request.getLink() != null ? request.getLink().trim() : null)
                .weight(weight)
                .position(position)
                .build();

        RoadmapStage saved = stageRepository.save(stage);
        return StageResponse.fromEntity(saved);
    }

    @Transactional
    public StageResponse updateStage(Long id, StageRequest request) {
        RoadmapStage stage = findStageEntity(id);

        Level targetLevel = stage.getLevel();
        if (request.getLevelId() != null && !request.getLevelId().equals(stage.getLevel().getId())) {
            targetLevel = levelService.findLevelEntity(request.getLevelId());
            stage.setLevel(targetLevel);
        }

        Long roadmapId = targetLevel.getRoadmap() != null ? targetLevel.getRoadmap().getId() : null;

        if (request.getCode() != null) {
            String code = request.getCode().trim();
            if (!code.isEmpty()) {
                if (roadmapId != null && stageRepository.existsByRoadmapIdAndCodeIgnoreCase(roadmapId, code, stage.getId())) {
                    throw new ConflictException("Stage code '" + code + "' is already in use in this roadmap.");
                }
                stage.setCode(code);
            } else {
                stage.setCode(null);
            }
        }

        if (request.getPriorityStageId() != null) {
            PriorityStage priorityStage = priorityStageService.findPriorityStageEntity(request.getPriorityStageId());
            stage.setPriorityStage(priorityStage);
        } else {
            stage.setPriorityStage(null);
        }

        if (request.getName() != null) {
            stage.setName(request.getName().trim());
        }
        if (request.getLink() != null) {
            stage.setLink(request.getLink().trim().isEmpty() ? null : request.getLink().trim());
        }
        if (request.getWeight() != null) {
            stage.setWeight(request.getWeight());
        }
        if (request.getPosition() != null && request.getPosition() > 0) {
            stage.setPosition(request.getPosition());
        }

        RoadmapStage updated = stageRepository.save(stage);
        return StageResponse.fromEntity(updated);
    }

    @Transactional
    public StageResponse updateStageWeight(Long id, BigDecimal weight) {
        RoadmapStage stage = findStageEntity(id);
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Stage weight must be greater than zero");
        }
        stage.setWeight(weight);
        RoadmapStage updated = stageRepository.save(stage);
        return StageResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteStage(Long id) {
        RoadmapStage stage = findStageEntity(id);
        stageRepository.delete(stage);
    }

    @Transactional
    public List<StageResponse> reorderStages(List<StageReorderRequest> requests) {
        for (StageReorderRequest req : requests) {
            RoadmapStage stage = findStageEntity(req.getId());
            stage.setPosition(req.getPosition());
            stageRepository.save(stage);
        }
        return requests.stream()
                .map(r -> StageResponse.fromEntity(findStageEntity(r.getId())))
                .toList();
    }

    public RoadmapStage findStageEntity(Long id) {
        return stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap Stage not found with ID: " + id));
    }
}

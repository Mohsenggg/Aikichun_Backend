package com.aikichun.domain.roadmap.service;

import com.aikichun.common.exception.ResourceNotFoundException;
import com.aikichun.domain.roadmap.dto.LevelReorderRequest;
import com.aikichun.domain.roadmap.dto.LevelRequest;
import com.aikichun.domain.roadmap.dto.LevelResponse;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.Roadmap;
import com.aikichun.domain.roadmap.repository.LevelRepository;
import com.aikichun.domain.roadmap.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;
    private final RoadmapRepository roadmapRepository;

    @Transactional
    public LevelResponse createLevel(LevelRequest request) {
        Roadmap roadmap = getActiveRoadmap();

        int position = (request.getPosition() != null && request.getPosition() > 0)
                ? request.getPosition()
                : levelRepository.findMaxPositionByRoadmapId(roadmap.getId()) + 1;

        BigDecimal weight = request.getWeight() != null ? request.getWeight() : java.math.BigDecimal.valueOf(10.00);

        Level level = Level.builder()
                .roadmap(roadmap)
                .name(request.getName().trim())
                .description(request.getDescription() != null && !request.getDescription().trim().isEmpty() ? request.getDescription().trim() : null)
                .position(position)
                .link(request.getLink() != null ? request.getLink().trim() : null)
                .weight(weight)
                .build();

        Level saved = levelRepository.save(level);
        return LevelResponse.fromEntity(saved);
    }

    @Transactional
    public LevelResponse updateLevel(Long id, LevelRequest request) {
        Level level = findLevelEntity(id);
        level.setName(request.getName().trim());
        if (request.getDescription() != null) {
            level.setDescription(request.getDescription().trim().isEmpty() ? null : request.getDescription().trim());
        }
        if (request.getPosition() != null && request.getPosition() > 0) {
            level.setPosition(request.getPosition());
        }
        if (request.getLink() != null) {
            level.setLink(request.getLink().trim().isEmpty() ? null : request.getLink().trim());
        }
        if (request.getWeight() != null) {
            level.setWeight(request.getWeight());
        }
        Level updated = levelRepository.save(level);
        return LevelResponse.fromEntity(updated);
    }

    @Transactional
    public LevelResponse updateLevelWeight(Long id, java.math.BigDecimal weight) {
        Level level = findLevelEntity(id);
        if (weight == null || weight.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Level weight must be greater than zero");
        }
        level.setWeight(weight);
        Level updated = levelRepository.save(level);
        return LevelResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteLevel(Long id) {
        Level level = findLevelEntity(id);
        levelRepository.delete(level);
    }

    @Transactional
    public List<LevelResponse> reorderLevels(List<LevelReorderRequest> requests) {
        Roadmap roadmap = getActiveRoadmap();
        for (LevelReorderRequest req : requests) {
            Level level = levelRepository.findByIdAndRoadmapId(req.getId(), roadmap.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + req.getId()));
            level.setPosition(req.getPosition());
            levelRepository.save(level);
        }

        return levelRepository.findByRoadmapIdOrderByPositionAsc(roadmap.getId()).stream()
                .map(LevelResponse::fromEntity)
                .toList();
    }

    public Level findLevelEntity(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with ID: " + id));
    }

    private Roadmap getActiveRoadmap() {
        return roadmapRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> {
                    Roadmap newRoadmap = Roadmap.builder()
                            .name("Default Learning Roadmap")
                            .description("Core dynamic learning curriculum")
                            .isActive(true)
                            .build();
                    return roadmapRepository.save(newRoadmap);
                });
    }
}

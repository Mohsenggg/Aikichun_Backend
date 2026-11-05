package com.aikichun.domain.roadmap.repository;

import com.aikichun.domain.roadmap.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    List<Level> findByRoadmapIdOrderByPositionAsc(Long roadmapId);

    Optional<Level> findByIdAndRoadmapId(Long id, Long roadmapId);

    @Query("SELECT COALESCE(MAX(l.position), 0) FROM Level l WHERE l.roadmap.id = :roadmapId")
    int findMaxPositionByRoadmapId(@Param("roadmapId") Long roadmapId);

    @Query("SELECT DISTINCT l FROM Level l " +
           "JOIN FETCH l.roadmap r " +
           "LEFT JOIN FETCH l.stages s " +
           "WHERE r.id = :roadmapId AND r.isActive = true ORDER BY l.position ASC")
    List<Level> findAllActiveLevelsWithHierarchyInRoadmap(@Param("roadmapId") Long roadmapId);
}

package com.aikichun.domain.roadmap.repository;

import com.aikichun.domain.roadmap.entity.RoadmapStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapStageRepository extends JpaRepository<RoadmapStage, Long> {

    List<RoadmapStage> findByLevelIdOrderByPositionAsc(Long levelId);

    Optional<RoadmapStage> findByIdAndLevelId(Long id, Long levelId);

    @Query("SELECT COALESCE(MAX(s.position), 0) FROM RoadmapStage s WHERE s.level.id = :levelId")
    int findMaxPositionByLevelId(@Param("levelId") Long levelId);

    @Query("SELECT COUNT(s) > 0 FROM RoadmapStage s " +
           "WHERE s.level.roadmap.id = :roadmapId " +
           "AND LOWER(s.code) = LOWER(:code) " +
           "AND (:excludeStageId IS NULL OR s.id != :excludeStageId)")
    boolean existsByRoadmapIdAndCodeIgnoreCase(@Param("roadmapId") Long roadmapId,
                                               @Param("code") String code,
                                               @Param("excludeStageId") Long excludeStageId);

    @Query("SELECT DISTINCT s FROM RoadmapStage s " +
           "JOIN FETCH s.level l " +
           "JOIN FETCH l.roadmap r " +
           "LEFT JOIN FETCH s.stageTasks st " +
           "LEFT JOIN FETCH st.task t " +
           "WHERE r.id = :roadmapId AND r.isActive = true")
    List<RoadmapStage> findAllActiveStagesWithTasksInRoadmap(@Param("roadmapId") Long roadmapId);
}

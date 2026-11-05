package com.aikichun.domain.roadmap.repository;

import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapStageTaskRepository extends JpaRepository<RoadmapStageTask, Long> {

    List<RoadmapStageTask> findByStageIdOrderByPositionAsc(Long stageId);

    Optional<RoadmapStageTask> findByStageIdAndTaskId(Long stageId, Long taskId);

    boolean existsByStageIdAndTaskId(Long stageId, Long taskId);

    @Query("SELECT COALESCE(MAX(st.position), 0) FROM RoadmapStageTask st WHERE st.stage.id = :stageId")
    int findMaxPositionByStageId(@Param("stageId") Long stageId);

    @Query("SELECT st FROM RoadmapStageTask st " +
           "JOIN FETCH st.task t " +
           "JOIN FETCH st.stage s " +
           "JOIN FETCH s.level l " +
           "JOIN FETCH l.roadmap r " +
           "WHERE r.id = :roadmapId AND r.isActive = true AND t.active = true")
    List<RoadmapStageTask> findAllActiveTasksInRoadmap(@Param("roadmapId") Long roadmapId);

    @Query("SELECT st FROM RoadmapStageTask st " +
           "JOIN FETCH st.stage s " +
           "JOIN FETCH s.level l " +
           "JOIN FETCH l.roadmap r " +
           "WHERE st.task.id = :taskId AND r.isActive = true")
    List<RoadmapStageTask> findActiveStageTasksByTaskId(@Param("taskId") Long taskId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoadmapStageTask st SET st.isMain = false WHERE st.stage.id = :stageId")
    void resetMainTaskInStage(@Param("stageId") Long stageId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoadmapStageTask st SET st.isMain = true WHERE st.stage.id = :stageId AND st.task.id = :taskId")
    void setMainTaskInStage(@Param("stageId") Long stageId, @Param("taskId") Long taskId);
}

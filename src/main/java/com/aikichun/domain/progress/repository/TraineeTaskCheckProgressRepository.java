package com.aikichun.domain.progress.repository;

import com.aikichun.domain.progress.entity.TraineeTaskCheckProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraineeTaskCheckProgressRepository extends JpaRepository<TraineeTaskCheckProgress, Long> {

    Optional<TraineeTaskCheckProgress> findByTraineeIdAndTaskIdAndCheckDefinitionId(
            UUID traineeId, Long taskId, Long checkDefinitionId);

    List<TraineeTaskCheckProgress> findByTraineeIdAndTaskId(UUID traineeId, Long taskId);

    @Query("SELECT cp FROM TraineeTaskCheckProgress cp " +
           "JOIN FETCH cp.checkDefinition cd " +
           "WHERE cp.traineeId = :traineeId AND cp.task.id IN :taskIds")
    List<TraineeTaskCheckProgress> findByTraineeIdAndTaskIdInWithDefinition(
            @Param("traineeId") UUID traineeId,
            @Param("taskIds") Collection<Long> taskIds);

    boolean existsByCheckDefinitionId(Long checkDefinitionId);

    void deleteByTaskId(Long taskId);
}

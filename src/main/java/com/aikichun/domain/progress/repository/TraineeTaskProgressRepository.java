package com.aikichun.domain.progress.repository;

import com.aikichun.domain.progress.entity.TraineeTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraineeTaskProgressRepository extends JpaRepository<TraineeTaskProgress, Long> {

    Optional<TraineeTaskProgress> findByTraineeIdAndTaskId(UUID traineeId, Long taskId);

    List<TraineeTaskProgress> findByTraineeIdAndTaskIdIn(UUID traineeId, Collection<Long> taskIds);

    List<TraineeTaskProgress> findByTraineeId(UUID traineeId);

    boolean existsByTaskId(Long taskId);
}

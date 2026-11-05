package com.aikichun.domain.roadmap.repository;

import com.aikichun.domain.roadmap.entity.PriorityStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityStageRepository extends JpaRepository<PriorityStage, Long> {
    List<PriorityStage> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}

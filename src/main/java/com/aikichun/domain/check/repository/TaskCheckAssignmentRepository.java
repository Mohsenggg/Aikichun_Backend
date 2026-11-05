package com.aikichun.domain.check.repository;

import com.aikichun.domain.check.entity.TaskCheckAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TaskCheckAssignmentRepository extends JpaRepository<TaskCheckAssignment, Long> {

    @Query("SELECT tca FROM TaskCheckAssignment tca JOIN FETCH tca.checkDefinition cd WHERE tca.task.id = :taskId ORDER BY cd.name ASC")
    List<TaskCheckAssignment> findByTaskIdWithDefinition(@Param("taskId") Long taskId);

    @Query("SELECT tca FROM TaskCheckAssignment tca JOIN FETCH tca.checkDefinition cd WHERE tca.task.id IN :taskIds ORDER BY cd.name ASC")
    List<TaskCheckAssignment> findByTaskIdInWithDefinition(@Param("taskIds") Collection<Long> taskIds);

    boolean existsByCheckDefinitionId(Long checkDefinitionId);

    long countByCheckDefinitionId(Long checkDefinitionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskCheckAssignment tca WHERE tca.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}

package com.aikichun.domain.check.repository;

import com.aikichun.domain.check.entity.CheckDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckDefinitionRepository extends JpaRepository<CheckDefinition, Long> {

    List<CheckDefinition> findAllByOrderByNameAsc();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CheckDefinition c " +
           "WHERE LOWER(c.name) = LOWER(:name) AND (:id IS NULL OR c.id != :id)")
    boolean existsByNameIgnoreCaseAndNotId(@Param("name") String name, @Param("id") Long id);
}

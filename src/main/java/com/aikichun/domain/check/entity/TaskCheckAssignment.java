package com.aikichun.domain.check.entity;

import com.aikichun.common.entity.BaseEntity;
import com.aikichun.domain.task.entity.Task;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "task_check_assignments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_task_check_assignment", columnNames = {"task_id", "check_definition_id"})
    },
    indexes = {
        @Index(name = "idx_assignment_task_id", columnList = "task_id"),
        @Index(name = "idx_assignment_check_id", columnList = "check_definition_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCheckAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_definition_id", nullable = false)
    private CheckDefinition checkDefinition;
}

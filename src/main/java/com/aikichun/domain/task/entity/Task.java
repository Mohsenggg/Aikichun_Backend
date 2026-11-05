package com.aikichun.domain.task.entity;

import com.aikichun.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "weight", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.valueOf(10.00);

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}

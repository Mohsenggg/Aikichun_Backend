package com.aikichun.domain.roadmap.entity;

import com.aikichun.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "roadmap_stages",
    indexes = {
        @Index(name = "idx_stages_level_id", columnList = "level_id"),
        @Index(name = "idx_stages_priority_stage_id", columnList = "priority_stage_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority_stage_id")
    private PriorityStage priorityStage;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "weight", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.valueOf(10.00);

    @Column(name = "position", nullable = false)
    private int position;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<RoadmapStageTask> stageTasks = new ArrayList<>();
}

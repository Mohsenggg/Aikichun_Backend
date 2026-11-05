package com.aikichun.domain.roadmap.service;

import com.aikichun.domain.check.dto.CheckDefinitionResponse;
import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.dto.TraineeCheckProgressResponse;
import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.entity.TaskProgressStatus;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.domain.roadmap.dto.TraineeLevelResponse;
import com.aikichun.domain.roadmap.dto.TraineeRoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.dto.TraineeStageResponse;
import com.aikichun.domain.roadmap.entity.AccessMode;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.Roadmap;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.RoadmapRepository;
import com.aikichun.domain.task.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeRoadmapHierarchyServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private TraineeProgressService traineeProgressService;

    @Mock
    private TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @InjectMocks
    private RoadmapService roadmapService;

    private final UUID traineeId = UUID.randomUUID();

    private Roadmap roadmap;
    private Level level1;
    private Level level2;
    private RoadmapStage stage1;
    private RoadmapStage stage2;
    private Task task1;
    private Task task2;
    private Task taskZeroCheck;

    @BeforeEach
    void setUp() {
        roadmap = Roadmap.builder()
                .name("Full Curriculum")
                .description("Test Description")
                .isActive(true)
                .accessMode(AccessMode.OPEN)
                .build();
        roadmap.setId(1L);

        level1 = Level.builder()
                .roadmap(roadmap)
                .name("Level 1 - Basics")
                .description("Fundamentals of the system")
                .weight(BigDecimal.valueOf(40.00))
                .position(1)
                .build();
        level1.setId(10L);

        level2 = Level.builder()
                .roadmap(roadmap)
                .name("Level 2 - Advanced")
                .description("Deep dive")
                .weight(BigDecimal.valueOf(60.00))
                .position(2)
                .build();
        level2.setId(20L);

        stage1 = RoadmapStage.builder()
                .level(level1)
                .name("Step 1.1")
                .weight(BigDecimal.valueOf(25.00))
                .position(1)
                .build();
        stage1.setId(100L);

        stage2 = RoadmapStage.builder()
                .level(level1)
                .name("Step 1.2")
                .weight(BigDecimal.valueOf(75.00))
                .position(2)
                .build();
        stage2.setId(101L);

        // Task 1 (weight 40, active, 2 checks)
        task1 = Task.builder().code("T-1").title("Task 1").weight(BigDecimal.valueOf(40.00)).active(true).build();
        task1.setId(1001L);

        // Task 2 (weight 60, active, 2 checks)
        task2 = Task.builder().code("T-2").title("Task 2").weight(BigDecimal.valueOf(60.00)).active(true).build();
        task2.setId(1002L);

        // Task Zero Check (weight 50, active, 0 checks - should be excluded from denominator)
        taskZeroCheck = Task.builder().code("T-0").title("Task Zero Check").weight(BigDecimal.valueOf(50.00)).active(true).build();
        taskZeroCheck.setId(1003L);

        RoadmapStageTask st1 = RoadmapStageTask.builder().stage(stage1).task(task1).position(1).isMain(true).build();
        RoadmapStageTask st2 = RoadmapStageTask.builder().stage(stage1).task(task2).position(2).isMain(false).build();
        RoadmapStageTask st3 = RoadmapStageTask.builder().stage(stage1).task(taskZeroCheck).position(3).isMain(false).build();

        stage1.setStageTasks(new ArrayList<>(List.of(st1, st2, st3)));
        stage2.setStageTasks(new ArrayList<>());

        level1.setStages(new ArrayList<>(List.of(stage1, stage2)));
        level2.setStages(new ArrayList<>());

        roadmap.setLevels(new ArrayList<>(List.of(level1, level2)));
    }

    @Test
    @DisplayName("Should assemble trainee hierarchy with computed progress, percentageOfLevel, stepsCount, and checks")
    void testGetTraineeRoadmapHierarchyComputations() {
        CheckDefinition def1 = CheckDefinition.builder().name("Knowledge").build();
        def1.setId(1L);
        CheckDefinition def2 = CheckDefinition.builder().name("Practice").build();
        def2.setId(2L);

        TaskCheckAssignment a1 = TaskCheckAssignment.builder().task(task1).checkDefinition(def1).build();
        TaskCheckAssignment a2 = TaskCheckAssignment.builder().task(task1).checkDefinition(def2).build();
        TaskCheckAssignment a3 = TaskCheckAssignment.builder().task(task2).checkDefinition(def1).build();
        TaskCheckAssignment a4 = TaskCheckAssignment.builder().task(task2).checkDefinition(def2).build();

        when(roadmapRepository.findFirstByIsActiveTrue()).thenReturn(Optional.of(roadmap));
        when(taskCheckAssignmentRepository.findByTaskIdInWithDefinition(anyCollection()))
                .thenReturn(List.of(a1, a2, a3, a4));

        // Task 1: 2/2 checks completed (100%) -> fraction 1.0 -> earned = 40.00
        TraineeTaskProgressResponse p1 = TraineeTaskProgressResponse.builder()
                .taskId(1001L)
                .completedChecksCount(2)
                .totalChecksCount(2)
                .progressPercentage(BigDecimal.valueOf(100.00))
                .status(TaskProgressStatus.COMPLETED)
                .checks(List.of(
                        TraineeCheckProgressResponse.builder().checkDefinitionId(1L).name("Knowledge").completed(true).build(),
                        TraineeCheckProgressResponse.builder().checkDefinitionId(2L).name("Practice").completed(true).build()
                ))
                .build();

        // Task 2: 1/2 checks completed (50%) -> fraction 0.5 -> earned = 30.00
        TraineeTaskProgressResponse p2 = TraineeTaskProgressResponse.builder()
                .taskId(1002L)
                .completedChecksCount(1)
                .totalChecksCount(2)
                .progressPercentage(BigDecimal.valueOf(50.00))
                .status(TaskProgressStatus.IN_PROGRESS)
                .checks(List.of(
                        TraineeCheckProgressResponse.builder().checkDefinitionId(1L).name("Knowledge").completed(true).build(),
                        TraineeCheckProgressResponse.builder().checkDefinitionId(2L).name("Practice").completed(false).build()
                ))
                .build();

        // Task Zero Check: 0 checks assigned -> excluded from denominator
        TraineeTaskProgressResponse pZero = TraineeTaskProgressResponse.builder()
                .taskId(1003L)
                .completedChecksCount(0)
                .totalChecksCount(0)
                .progressPercentage(BigDecimal.ZERO)
                .status(TaskProgressStatus.NOT_STARTED)
                .checks(List.of())
                .build();

        when(traineeProgressService.getProgressMapForTrainee(eq(traineeId), anyCollection()))
                .thenReturn(Map.of(1001L, p1, 1002L, p2, 1003L, pZero));

        when(traineeProgressService.calculateRoadmapProgress(traineeId, 1L))
                .thenReturn(TraineeRoadmapProgressResponse.builder()
                        .traineeId(traineeId)
                        .roadmapId(1L)
                        .overallProgressPercentage(BigDecimal.valueOf(70.00))
                        .build());

        TraineeRoadmapHierarchyResponse response = roadmapService.getTraineeRoadmapHierarchy(traineeId);

        assertNotNull(response);
        assertEquals(AccessMode.OPEN, response.getAccessMode());
        assertEquals(2, response.getLevels().size());

        TraineeLevelResponse lvl1 = response.getLevels().get(0);
        assertEquals("Level 1 - Basics", lvl1.getName());
        assertEquals("Fundamentals of the system", lvl1.getDescription());
        assertEquals(2, lvl1.getStepsCount());
        assertFalse(lvl1.isLocked());

        // Stage 1.1: weight 25, Stage 1.2: weight 75 -> total stage weight = 100
        // Step 1.1 percentageOfLevel = 25 / 100 * 100 = 25.00%
        // Step 1.2 percentageOfLevel = 75 / 100 * 100 = 75.00%
        TraineeStageResponse stg1 = lvl1.getStages().get(0);
        TraineeStageResponse stg2 = lvl1.getStages().get(1);
        assertEquals(new BigDecimal("25.00"), stg1.getPercentageOfLevel());
        assertEquals(new BigDecimal("75.00"), stg2.getPercentageOfLevel());

        // Step 1.1 progress:
        // Total task weight with checks = 40 + 60 = 100 (taskZeroCheck excluded)
        // Earned task weight = 40 * 1.0 + 60 * 0.5 = 70.00
        // Step 1.1 progress % = 70.00%
        assertEquals(new BigDecimal("70.00"), stg1.getProgressPercentage());

        // Step 1.2 progress = 0.00% (no tasks)
        assertEquals(new BigDecimal("0.00"), stg2.getProgressPercentage());

        // Level 1 progress:
        // Total stage weight = 100
        // Earned = 25 * 0.70 + 75 * 0.0 = 17.50
        // Level 1 progress % = 17.50%
        assertEquals(new BigDecimal("17.50"), lvl1.getProgressPercentage());

        // Verify TaskResponse checks are populated
        assertEquals(2, stg1.getTasks().get(0).getTask().getChecks().size());
        assertEquals("Knowledge", stg1.getTasks().get(0).getTask().getChecks().get(0).getName());
    }

    @Test
    @DisplayName("Should lock higher levels when AccessMode is PROGRESSIVE")
    void testProgressiveAccessModeLocking() {
        roadmap.setAccessMode(AccessMode.PROGRESSIVE);

        when(roadmapRepository.findFirstByIsActiveTrue()).thenReturn(Optional.of(roadmap));
        when(taskCheckAssignmentRepository.findByTaskIdInWithDefinition(anyCollection()))
                .thenReturn(List.of());
        when(traineeProgressService.getProgressMapForTrainee(eq(traineeId), anyCollection()))
                .thenReturn(Map.of());
        when(traineeProgressService.calculateRoadmapProgress(traineeId, 1L))
                .thenReturn(TraineeRoadmapProgressResponse.builder().build());

        TraineeRoadmapHierarchyResponse response = roadmapService.getTraineeRoadmapHierarchy(traineeId);

        assertEquals(AccessMode.PROGRESSIVE, response.getAccessMode());
        // Level 1 is incomplete (progress < 100%) -> Level 1 is UNLOCKED
        assertFalse(response.getLevels().get(0).isLocked());
        // Level 2 position > Level 1 position -> Level 2 is LOCKED
        assertTrue(response.getLevels().get(1).isLocked());
    }
}


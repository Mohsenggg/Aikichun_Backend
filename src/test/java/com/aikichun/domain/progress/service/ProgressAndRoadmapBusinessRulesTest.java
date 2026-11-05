package com.aikichun.domain.progress.service;

import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.progress.dto.TraineeRoadmapProgressResponse;
import com.aikichun.domain.progress.entity.TaskProgressStatus;
import com.aikichun.domain.progress.entity.TraineeTaskCheckProgress;
import com.aikichun.domain.progress.entity.TraineeTaskProgress;
import com.aikichun.domain.progress.repository.TraineeTaskCheckProgressRepository;
import com.aikichun.domain.progress.repository.TraineeTaskProgressRepository;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.Roadmap;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.LevelRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageTaskRepository;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.service.TaskService;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressAndRoadmapBusinessRulesTest {

    @Mock
    private TraineeTaskProgressRepository progressRepository;

    @Mock
    private RoadmapStageTaskRepository stageTaskRepository;

    @Mock
    private TraineeTaskCheckProgressRepository traineeTaskCheckProgressRepository;

    @Mock
    private TaskCheckAssignmentRepository taskCheckAssignmentRepository;

    @Mock
    private CheckDefinitionRepository checkDefinitionRepository;

    @Mock
    private RoadmapStageRepository stageRepository;

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TraineeProgressService progressService;

    private final UUID traineeId = UUID.randomUUID();

    private Roadmap roadmap;
    private Level level;
    private RoadmapStage stage;
    private Task taskA;
    private Task taskB;
    private Task taskC;
    private RoadmapStageTask stageTaskA;
    private RoadmapStageTask stageTaskB;
    private RoadmapStageTask stageTaskC;

    @BeforeEach
    void setUp() {
        roadmap = Roadmap.builder().name("Core Curriculum").isActive(true).build();
        roadmap.setId(1L);

        level = Level.builder().roadmap(roadmap).name("Level 1").weight(BigDecimal.valueOf(10.00)).position(1).build();
        level.setId(1L);

        stage = RoadmapStage.builder().level(level).name("Basic Configuration").weight(BigDecimal.valueOf(10.00)).position(1).build();
        stage.setId(1L);

        // Task A: weight 20
        taskA = Task.builder().code("T-01").title("Task A").weight(BigDecimal.valueOf(20.00)).active(true).build();
        taskA.setId(101L);

        // Task B: weight 30
        taskB = Task.builder().code("T-02").title("Task B").weight(BigDecimal.valueOf(30.00)).active(true).build();
        taskB.setId(102L);

        // Task C: weight 50
        taskC = Task.builder().code("T-03").title("Task C").weight(BigDecimal.valueOf(50.00)).active(true).build();
        taskC.setId(103L);

        stageTaskA = RoadmapStageTask.builder().stage(stage).task(taskA).position(1).isMain(false).build();
        stageTaskB = RoadmapStageTask.builder().stage(stage).task(taskB).position(2).isMain(true).build();
        stageTaskC = RoadmapStageTask.builder().stage(stage).task(taskC).position(3).isMain(false).build();

        stage.setStageTasks(new ArrayList<>(List.of(stageTaskA, stageTaskB, stageTaskC)));
        level.setStages(new ArrayList<>(List.of(stage)));
    }

    @Test
    @DisplayName("Should dynamically calculate weighted roadmap progress correctly")
    void testWeightedRoadmapProgressCalculation() {
        CheckDefinition def1 = CheckDefinition.builder().name("Check 1").build();
        def1.setId(1L);
        CheckDefinition def2 = CheckDefinition.builder().name("Check 2").build();
        def2.setId(2L);
        CheckDefinition def3 = CheckDefinition.builder().name("Check 3").build();
        def3.setId(3L);
        CheckDefinition def4 = CheckDefinition.builder().name("Check 4").build();
        def4.setId(4L);

        // Task A: 4 checks, all 4 completed (100%)
        TaskCheckAssignment a1 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def1).build();
        TaskCheckAssignment a2 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def2).build();
        TaskCheckAssignment a3 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def3).build();
        TaskCheckAssignment a4 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def4).build();

        // Task B: 4 checks, 2 completed (50%)
        TaskCheckAssignment b1 = TaskCheckAssignment.builder().task(taskB).checkDefinition(def1).build();
        TaskCheckAssignment b2 = TaskCheckAssignment.builder().task(taskB).checkDefinition(def2).build();
        TaskCheckAssignment b3 = TaskCheckAssignment.builder().task(taskB).checkDefinition(def3).build();
        TaskCheckAssignment b4 = TaskCheckAssignment.builder().task(taskB).checkDefinition(def4).build();

        // Task C: 4 checks, 0 completed (0%)
        TaskCheckAssignment c1 = TaskCheckAssignment.builder().task(taskC).checkDefinition(def1).build();
        TaskCheckAssignment c2 = TaskCheckAssignment.builder().task(taskC).checkDefinition(def2).build();
        TaskCheckAssignment c3 = TaskCheckAssignment.builder().task(taskC).checkDefinition(def3).build();
        TaskCheckAssignment c4 = TaskCheckAssignment.builder().task(taskC).checkDefinition(def4).build();

        // Check progress records for Trainee
        TraineeTaskCheckProgress cpA1 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def1).completed(true).build();
        TraineeTaskCheckProgress cpA2 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def2).completed(true).build();
        TraineeTaskCheckProgress cpA3 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def3).completed(true).build();
        TraineeTaskCheckProgress cpA4 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def4).completed(true).build();

        TraineeTaskCheckProgress cpB1 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskB).checkDefinition(def1).completed(true).build();
        TraineeTaskCheckProgress cpB2 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskB).checkDefinition(def2).completed(true).build();

        when(levelRepository.findAllActiveLevelsWithHierarchyInRoadmap(1L))
                .thenReturn(List.of(level));

        when(taskCheckAssignmentRepository.findByTaskIdInWithDefinition(anyCollection()))
                .thenReturn(List.of(a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4));

        when(traineeTaskCheckProgressRepository.findByTraineeIdAndTaskIdInWithDefinition(eq(traineeId), anyCollection()))
                .thenReturn(List.of(cpA1, cpA2, cpA3, cpA4, cpB1, cpB2));

        // Calculation:
        // Total task weight = 20 + 30 + 50 = 100
        // Task A contribution: 20 * (4/4) = 20
        // Task B contribution: 30 * (2/4) = 15
        // Task C contribution: 50 * (0/4) = 0
        // Earned task weight in step = 35.00
        // Step progress fraction = 35 / 100 = 0.35 (35%)
        // Level progress fraction = 0.35 (35%)
        // Overall roadmap = 35.00%
        TraineeRoadmapProgressResponse response = progressService.calculateRoadmapProgress(traineeId, 1L);

        assertEquals(new BigDecimal("35.00"), response.getOverallProgressPercentage());
        assertEquals(new BigDecimal("10.00"), response.getTotalCurriculumWeight());
        assertEquals(new BigDecimal("3.50"), response.getEarnedWeightedPoints());
        assertEquals(3, response.getTotalTasksCount());
        assertEquals(1, response.getCompletedTasksCount());
        assertEquals(1, response.getInProgressTasksCount());
    }

    @Test
    @DisplayName("Adding a new task to roadmap automatically normalizes denominator without modifying trainee records")
    void testRoadmapEvolutionWithNewTask() {
        CheckDefinition def1 = CheckDefinition.builder().name("Check 1").build();
        def1.setId(1L);

        // Coach adds Task D: weight 20
        Task taskD = Task.builder().code("T-04").title("Task D").weight(BigDecimal.valueOf(20.00)).active(true).build();
        taskD.setId(104L);
        RoadmapStageTask stageTaskD = RoadmapStageTask.builder().stage(stage).task(taskD).position(4).isMain(false).build();

        stage.setStageTasks(new ArrayList<>(List.of(stageTaskA, stageTaskB, stageTaskC, stageTaskD)));

        // Task A (20): 1 check, completed 100%
        TaskCheckAssignment a1 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def1).build();
        // Task B (30): 1 check, completed 100%
        TaskCheckAssignment b1 = TaskCheckAssignment.builder().task(taskB).checkDefinition(def1).build();
        // Task C (50): 1 check, 0%
        TaskCheckAssignment c1 = TaskCheckAssignment.builder().task(taskC).checkDefinition(def1).build();
        // Task D (20): 1 check, 0%
        TaskCheckAssignment d1 = TaskCheckAssignment.builder().task(taskD).checkDefinition(def1).build();

        TraineeTaskCheckProgress cpA1 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def1).completed(true).build();
        TraineeTaskCheckProgress cpB1 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskB).checkDefinition(def1).completed(true).build();

        when(levelRepository.findAllActiveLevelsWithHierarchyInRoadmap(1L))
                .thenReturn(List.of(level));

        when(taskCheckAssignmentRepository.findByTaskIdInWithDefinition(anyCollection()))
                .thenReturn(List.of(a1, b1, c1, d1));

        when(traineeTaskCheckProgressRepository.findByTraineeIdAndTaskIdInWithDefinition(eq(traineeId), anyCollection()))
                .thenReturn(List.of(cpA1, cpB1));

        // Earned = 20 * 1.0 + 30 * 1.0 = 50
        // Total tasks weight = 120
        // Step progress = (50 / 120) * 100 = 41.67%
        // Level has 1 step (weight 10), Roadmap has 1 level (weight 10)
        // Earned points = 4.17
        TraineeRoadmapProgressResponse response = progressService.calculateRoadmapProgress(traineeId, 1L);

        assertEquals(new BigDecimal("41.67"), response.getOverallProgressPercentage());
        assertEquals(new BigDecimal("10.00"), response.getTotalCurriculumWeight());
        assertEquals(new BigDecimal("4.17"), response.getEarnedWeightedPoints());
        assertEquals(4, response.getTotalTasksCount());
        assertEquals(2, response.getCompletedTasksCount());
    }

    @Test
    @DisplayName("Tasks with zero assigned checks are excluded from step progress calculation denominator")
    void testZeroCheckTaskExclusionFromDenominator() {
        CheckDefinition def1 = CheckDefinition.builder().name("Check 1").build();
        def1.setId(1L);

        // Task A (20): 1 check, completed (100%)
        TaskCheckAssignment a1 = TaskCheckAssignment.builder().task(taskA).checkDefinition(def1).build();
        // Task B (30): 0 checks assigned!
        // Task C (50): 0 checks assigned!

        TraineeTaskCheckProgress cpA1 = TraineeTaskCheckProgress.builder().traineeId(traineeId).task(taskA).checkDefinition(def1).completed(true).build();

        when(levelRepository.findAllActiveLevelsWithHierarchyInRoadmap(1L))
                .thenReturn(List.of(level));

        when(taskCheckAssignmentRepository.findByTaskIdInWithDefinition(anyCollection()))
                .thenReturn(List.of(a1));

        when(traineeTaskCheckProgressRepository.findByTraineeIdAndTaskIdInWithDefinition(eq(traineeId), anyCollection()))
                .thenReturn(List.of(cpA1));

        // Calculation:
        // Only Task A has checks (weight 20, completed 100%).
        // Task B (30) and Task C (50) have 0 checks, so they are excluded from denominator.
        // Step progress fraction = 20 / 20 = 1.0 (100%)
        // Level progress fraction = 1.0 (100%)
        // Overall roadmap = 100.00%
        TraineeRoadmapProgressResponse response = progressService.calculateRoadmapProgress(traineeId, 1L);

        assertEquals(new BigDecimal("100.00"), response.getOverallProgressPercentage());
        assertEquals(new BigDecimal("10.00"), response.getTotalCurriculumWeight());
        assertEquals(new BigDecimal("10.00"), response.getEarnedWeightedPoints());
        assertEquals(3, response.getTotalTasksCount());
        assertEquals(1, response.getCompletedTasksCount());
    }
}


package com.aikichun.config;

import com.aikichun.domain.check.entity.CheckDefinition;
import com.aikichun.domain.check.entity.TaskCheckAssignment;
import com.aikichun.domain.check.repository.CheckDefinitionRepository;
import com.aikichun.domain.check.repository.TaskCheckAssignmentRepository;
import com.aikichun.domain.roadmap.entity.AccessMode;
import com.aikichun.domain.roadmap.entity.Level;
import com.aikichun.domain.roadmap.entity.PriorityStage;
import com.aikichun.domain.roadmap.entity.Roadmap;
import com.aikichun.domain.roadmap.entity.RoadmapStage;
import com.aikichun.domain.roadmap.entity.RoadmapStageTask;
import com.aikichun.domain.roadmap.repository.LevelRepository;
import com.aikichun.domain.roadmap.repository.PriorityStageRepository;
import com.aikichun.domain.roadmap.repository.RoadmapRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageRepository;
import com.aikichun.domain.roadmap.repository.RoadmapStageTaskRepository;
import com.aikichun.domain.task.entity.Task;
import com.aikichun.domain.task.repository.TaskRepository;
import com.aikichun.user.entites.AccountStatus;
import com.aikichun.user.entites.Branch;
import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;
import com.aikichun.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoadmapRepository roadmapRepository;
    private final LevelRepository levelRepository;
    private final PriorityStageRepository priorityStageRepository;
    private final RoadmapStageRepository roadmapStageRepository;
    private final TaskRepository taskRepository;
    private final CheckDefinitionRepository checkDefinitionRepository;
    private final TaskCheckAssignmentRepository taskCheckAssignmentRepository;
    private final RoadmapStageTaskRepository roadmapStageTaskRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking data initialization state...");
        seedUsers();
        seedDefaultRoadmap();
    }

    private void seedUsers() {
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);
        LocalDate oneYearOut = today.plusYears(1);

        // 1. Admin
        if (userRepository.findByEmail("admin@aikichun.com").isEmpty()) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email("admin@aikichun.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .dateOfBirth(LocalDate.of(1985, 1, 1))
                    .profession("Head Coach")
                    .branch(Branch.SIX_OF_OCTOBER)
                    .phoneNumber("+201000000001")
                    .phoneNumberEmergency("+201000000002")
                    .role(Role.ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .subscriptionStartMonth(1)
                    .subscriptionStartYear(today.getYear())
                    .subscriptionStartDate(startOfYear)
                    .subscriptionEndDate(oneYearOut)
                    .build();
            userRepository.save(admin);
            log.info("Seeded Admin user: admin@aikichun.com");
        }

        // 2. Moderator
        if (userRepository.findByEmail("moderator@aikichun.com").isEmpty()) {
            User moderator = User.builder()
                    .fullName("Club Moderator")
                    .email("moderator@aikichun.com")
                    .passwordHash(passwordEncoder.encode("Moderator@123"))
                    .dateOfBirth(LocalDate.of(1992, 5, 15))
                    .profession("Club Manager")
                    .branch(Branch.SIX_OF_OCTOBER)
                    .phoneNumber("+201000000003")
                    .phoneNumberEmergency("+201000000004")
                    .role(Role.MODERATOR)
                    .accountStatus(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .subscriptionStartMonth(1)
                    .subscriptionStartYear(today.getYear())
                    .subscriptionStartDate(startOfYear)
                    .subscriptionEndDate(oneYearOut)
                    .build();
            userRepository.save(moderator);
            log.info("Seeded Moderator user: moderator@aikichun.com");
        }

        // 3. Trainee
        if (userRepository.findByEmail("trainee@aikichun.com").isEmpty()) {
            User trainee = User.builder()
                    .fullName("Test Trainee")
                    .email("trainee@aikichun.com")
                    .passwordHash(passwordEncoder.encode("Trainee@123"))
                    .dateOfBirth(LocalDate.of(2000, 8, 20))
                    .profession("Student")
                    .branch(Branch.SIX_OF_OCTOBER)
                    .phoneNumber("+201000000005")
                    .phoneNumberEmergency("+201000000006")
                    .role(Role.MEMBER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .subscriptionStartMonth(1)
                    .subscriptionStartYear(today.getYear())
                    .subscriptionStartDate(startOfYear)
                    .subscriptionEndDate(oneYearOut)
                    .build();
            userRepository.save(trainee);
            log.info("Seeded Trainee user: trainee@aikichun.com");
        }
    }

    private void seedDefaultRoadmap() {
        if (roadmapRepository.count() > 0) {
            log.info("Roadmap already exists, skipping default roadmap seeding.");
            return;
        }

        log.info("Seeding default learning roadmap curriculum...");

        // 1. Priority Stage
        PriorityStage priorityStage = PriorityStage.builder()
                .name("Core Fundamentals")
                .color("#22c55e")
                .description("Fundamental core syllabus techniques")
                .build();
        priorityStage = priorityStageRepository.save(priorityStage);

        // 2. Roadmap
        Roadmap roadmap = Roadmap.builder()
                .name("Aikido Foundation Curriculum")
                .description("Core dynamic learning and belt progression curriculum")
                .isActive(true)
                .accessMode(AccessMode.OPEN)
                .build();
        roadmap = roadmapRepository.save(roadmap);

        // 3. Level 1
        Level level1 = Level.builder()
                .roadmap(roadmap)
                .name("Level 1 — 6th Kyu (White Belt)")
                .description("Foundational stances, balance, and basic joint controls")
                .position(0)
                .weight(BigDecimal.valueOf(10.00))
                .build();
        level1 = levelRepository.save(level1);

        // 4. Stage
        RoadmapStage stage1 = RoadmapStage.builder()
                .level(level1)
                .priorityStage(priorityStage)
                .code("6K-STG-01")
                .name("Kamae & Tai Sabaki (Stances & Body Movement)")
                .position(0)
                .weight(BigDecimal.valueOf(10.00))
                .build();
        stage1 = roadmapStageRepository.save(stage1);

        // 5. Task
        Task task1 = Task.builder()
                .code("6K-TSK-01")
                .title("Shomenuchi Ikkyo (Omote)")
                .description("First teaching: direct entry against straight strike to the head")
                .weight(BigDecimal.valueOf(10.00))
                .active(true)
                .build();
        task1 = taskRepository.save(task1);

        // 6. Check Definitions
        CheckDefinition check1 = CheckDefinition.builder()
                .name("Posture & Centerline (Shisei & Seichusen)")
                .description("Maintains upright posture and controls the centerline during execution")
                .build();
        check1 = checkDefinitionRepository.save(check1);

        CheckDefinition check2 = CheckDefinition.builder()
                .name("Off-Balancing (Kuzushi)")
                .description("Effectively unbalances uke before entering and initiating the pin")
                .build();
        check2 = checkDefinitionRepository.save(check2);

        // 7. Check Assignments
        TaskCheckAssignment assign1 = TaskCheckAssignment.builder()
                .task(task1)
                .checkDefinition(check1)
                .build();
        taskCheckAssignmentRepository.save(assign1);

        TaskCheckAssignment assign2 = TaskCheckAssignment.builder()
                .task(task1)
                .checkDefinition(check2)
                .build();
        taskCheckAssignmentRepository.save(assign2);

        // 8. Stage Task Link
        RoadmapStageTask stageTask = RoadmapStageTask.builder()
                .stage(stage1)
                .task(task1)
                .position(0)
                .isMain(true)
                .build();
        roadmapStageTaskRepository.save(stageTask);

        log.info("Default learning roadmap seeded successfully.");
    }
}


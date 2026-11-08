package com.aikichun.security;

import com.aikichun.Security.CustomUserDetailsService;
import com.aikichun.Security.JwtAuthenticationFilter;
import com.aikichun.Security.UserPrincipal;
import com.aikichun.auth.AuthController;
import com.aikichun.auth.AuthService;
import com.aikichun.auth.JwtService;
import com.aikichun.auth.OtpService;
import com.aikichun.auth.dto.AuthResponse;
import com.aikichun.auth.dto.LoginRequest;
import com.aikichun.auth.dto.PublicUserDto;
import com.aikichun.domain.check.controller.CoachCheckDefinitionController;
import com.aikichun.domain.check.service.CheckDefinitionService;
import com.aikichun.domain.progress.controller.TraineeProgressController;
import com.aikichun.domain.progress.dto.TraineeCheckProgressRequest;
import com.aikichun.domain.progress.dto.TraineeTaskProgressResponse;
import com.aikichun.domain.progress.service.TraineeProgressService;
import com.aikichun.domain.roadmap.controller.TraineeRoadmapController;
import com.aikichun.domain.roadmap.dto.TraineeRoadmapHierarchyResponse;
import com.aikichun.domain.roadmap.service.RoadmapService;
import com.aikichun.exception.GlobalExceptionHandler;
import com.aikichun.exception.InvalidCredentialsException;
import com.aikichun.user.UserService;
import com.aikichun.user.entites.AccountStatus;
import com.aikichun.user.entites.Branch;
import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;
import com.aikichun.user.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityAuthorizationIntegrationTest {

    private MockMvc mockMvc;
    private JwtService jwtService;
    private CustomUserDetailsService customUserDetailsService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraineeProgressService traineeProgressService;

    @Mock
    private RoadmapService roadmapService;

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    @Mock
    private UserService userService;

    @Mock
    private CheckDefinitionService checkDefinitionService;

    private UUID traineeAId;
    private UUID traineeBId;
    private UUID moderatorId;
    private UUID adminId;

    private User traineeAUser;
    private User traineeBUser;
    private User moderatorUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "expirationHours", 12L);
        jwtService.init();

        customUserDetailsService = new CustomUserDetailsService(userRepository);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, customUserDetailsService);

        TraineeProgressController traineeProgressController = new TraineeProgressController(traineeProgressService);
        TraineeRoadmapController traineeRoadmapController = new TraineeRoadmapController(roadmapService, traineeProgressService);
        AuthController authController = new AuthController(authService, jwtService, otpService, userRepository, userService);
        CoachCheckDefinitionController coachCheckDefinitionController = new CoachCheckDefinitionController(checkDefinitionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(traineeProgressController, traineeRoadmapController, authController, coachCheckDefinitionController)
                .addFilters(jwtAuthenticationFilter)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        traineeAId = UUID.randomUUID();
        traineeBId = UUID.randomUUID();
        moderatorId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        LocalDate today = LocalDate.now();

        traineeAUser = User.builder()
                .id(traineeAId)
                .email("traineeA@aikichun.com")
                .passwordHash("hashed")
                .role(Role.MEMBER)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptionStartDate(today.minusMonths(1))
                .subscriptionEndDate(today.plusMonths(11))
                .branch(Branch.SIX_OF_OCTOBER)
                .build();

        traineeBUser = User.builder()
                .id(traineeBId)
                .email("traineeB@aikichun.com")
                .passwordHash("hashed")
                .role(Role.MEMBER)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptionStartDate(today.minusMonths(1))
                .subscriptionEndDate(today.plusMonths(11))
                .branch(Branch.SIX_OF_OCTOBER)
                .build();

        moderatorUser = User.builder()
                .id(moderatorId)
                .email("moderator@aikichun.com")
                .passwordHash("hashed")
                .role(Role.MODERATOR)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptionStartDate(today.minusMonths(1))
                .subscriptionEndDate(today.plusMonths(11))
                .branch(Branch.SIX_OF_OCTOBER)
                .build();

        adminUser = User.builder()
                .id(adminId)
                .email("admin@aikichun.com")
                .passwordHash("hashed")
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .subscriptionStartDate(today.minusMonths(1))
                .subscriptionEndDate(today.plusMonths(11))
                .branch(Branch.SIX_OF_OCTOBER)
                .build();

        when(userRepository.findById(traineeAId)).thenReturn(Optional.of(traineeAUser));
        when(userRepository.findById(traineeBId)).thenReturn(Optional.of(traineeBUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("1. Blocked user using previously issued JWT is immediately rejected with 401 at filter level")
    void whenUserBecomesBlocked_thenPreviouslyIssuedJwtReturns401() throws Exception {
        // Issue token while active
        String token = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");

        // Now user is updated to BLOCKED in DB
        traineeAUser.setAccountStatus(AccountStatus.BLOCKED);

        mockMvc.perform(get("/api/v1/trainee/" + traineeAId + "/roadmap")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Account is blocked. Please contact support."));
    }

    @Test
    @DisplayName("2. Blocked user login attempt is rejected with 401")
    void whenBlockedUserLogsIn_thenReturns401() throws Exception {
        LoginRequest request = new LoginRequest("blocked@aikichun.com", "Password@123");
        when(authService.login(request.email(), request.password()))
                .thenThrow(new InvalidCredentialsException("Account is blocked. Please contact support."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Account is blocked. Please contact support."));
    }

    @Test
    @DisplayName("3. Pending verification user login returns 200 with null token and PENDING_VERIFICATION status")
    void whenPendingVerificationUserLogsIn_thenReturns200WithNullToken() throws Exception {
        LoginRequest request = new LoginRequest("pending@aikichun.com", "Password@123");
        PublicUserDto publicUser = new PublicUserDto(
                UUID.randomUUID(), "pending@aikichun.com", "Pending User", Role.MEMBER, AccountStatus.PENDING_VERIFICATION
        );
        AuthResponse response = new AuthResponse(publicUser, null, null, null);

        when(authService.login(request.email(), request.password())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user.accountStatus").value("PENDING_VERIFICATION"));
    }

    @Test
    @DisplayName("4. Active trainee updating own check progress returns 200 OK")
    void whenActiveTraineeUpdatesOwnProgress_thenReturns200() throws Exception {
        String token = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");
        TraineeCheckProgressRequest request = new TraineeCheckProgressRequest(true);

        when(traineeProgressService.toggleCheckProgress(eq(traineeAId), eq(1L), eq(10L), eq(true)))
                .thenReturn(TraineeTaskProgressResponse.builder()
                        .traineeId(traineeAId)
                        .taskId(1L)
                        .build());

        mockMvc.perform(put("/api/v1/trainee/" + traineeAId + "/tasks/1/checks/10")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("5. Inactive trainee updating check progress is rejected with 422 Unprocessable Entity")
    void whenInactiveTraineeUpdatesProgress_thenReturns422() throws Exception {
        traineeAUser.setAccountStatus(AccountStatus.INACTIVE);
        String token = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");
        TraineeCheckProgressRequest request = new TraineeCheckProgressRequest(true);

        mockMvc.perform(put("/api/v1/trainee/" + traineeAId + "/tasks/1/checks/10")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Subscription is inactive or expired. Please renew to record progress."));
    }

    @Test
    @DisplayName("6. Active trainee with expired subscription date is rejected with 422 on progress modification")
    void whenActiveTraineeWithExpiredSubscriptionDate_thenReturns422() throws Exception {
        // Status is ACTIVE, but subscriptionEndDate is in the past!
        traineeAUser.setAccountStatus(AccountStatus.ACTIVE);
        traineeAUser.setSubscriptionStartDate(LocalDate.now().minusMonths(6));
        traineeAUser.setSubscriptionEndDate(LocalDate.now().minusDays(1)); // Expired yesterday

        String token = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");
        TraineeCheckProgressRequest request = new TraineeCheckProgressRequest(true);

        mockMvc.perform(put("/api/v1/trainee/" + traineeAId + "/tasks/1/checks/10")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Subscription is inactive or expired. Please renew to record progress."));
    }

    @Test
    @DisplayName("7. Inactive trainee can read own roadmap hierarchy with 200 OK")
    void whenInactiveTraineeReadsRoadmap_thenReturns200() throws Exception {
        traineeAUser.setAccountStatus(AccountStatus.INACTIVE);
        String token = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");

        when(roadmapService.getTraineeRoadmapHierarchy(traineeAId))
                .thenReturn(TraineeRoadmapHierarchyResponse.builder().build());

        mockMvc.perform(get("/api/v1/trainee/" + traineeAId + "/roadmap")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("8. Trainee A attempting to modify Trainee B's progress returns 403 Forbidden (IDOR prevention)")
    void whenTraineeAAttemptsToModifyTraineeB_thenReturns403() throws Exception {
        // Trainee A token, but calling Trainee B's endpoint URL
        String tokenA = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");
        TraineeCheckProgressRequest request = new TraineeCheckProgressRequest(true);

        mockMvc.perform(put("/api/v1/trainee/" + traineeBId + "/tasks/1/checks/10")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: You cannot view or modify another trainee's data."));
    }

    @Test
    @DisplayName("9. Trainee A attempting to read Trainee B's roadmap returns 403 Forbidden (IDOR prevention)")
    void whenTraineeAAttemptsToReadTraineeBRoadmap_thenReturns403() throws Exception {
        String tokenA = jwtService.generateToken(traineeAId, traineeAUser.getEmail(), "MEMBER");

        mockMvc.perform(get("/api/v1/trainee/" + traineeBId + "/roadmap")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied: You cannot view or modify another trainee's data."));
    }

    @Test
    @DisplayName("10. Token role claim extraction and verification")
    void whenTokenGeneratedWithRole_thenRoleClaimExtractedCorrectly() {
        String adminToken = jwtService.generateToken(adminId, "admin@aikichun.com", "ADMIN");
        String extractedRole = jwtService.extractRole(adminToken);
        org.junit.jupiter.api.Assertions.assertEquals("ADMIN", extractedRole);
    }
}

package com.aikichun.auth;

import com.aikichun.auth.dto.*;
import com.aikichun.user.UserService;
import com.aikichun.user.dto.UserDto;
import com.aikichun.user.entites.AccountStatus;
import com.aikichun.user.entites.User;
import com.aikichun.user.repo.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam LocalDate dateOfBirth,
            @RequestParam(required = false) String profession,
            @RequestParam String branch,
            @RequestParam String phoneNumber,
            @RequestParam String phoneNumberEmergency,
            @RequestParam(required = false) String servicesOrExperience,
            @RequestParam(required = false) String productsAndDiscounts,
            @RequestParam(required = false) String martialArtsExperience,
            @RequestParam Boolean hasChronicConditionOrInjury,
            @RequestParam(required = false) String medicalNotes,
            @RequestParam Integer subscriptionStartMonth,
            @RequestParam Integer subscriptionStartYear,
            @RequestParam MultipartFile profilePhoto,
            @RequestParam MultipartFile idPhoto
    ) {
        log.info("Register endpoint called - email={}, branch={}", email, branch);
        RegisterRequest request = new RegisterRequest(
                fullName, email, password, dateOfBirth, profession, branch,
                phoneNumber, phoneNumberEmergency, servicesOrExperience,
                productsAndDiscounts, martialArtsExperience, hasChronicConditionOrInjury,
                medicalNotes, subscriptionStartMonth, subscriptionStartYear,
                profilePhoto, idPhoto
        );
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("Login endpoint called - email={}", loginRequest.email());
        return ResponseEntity.ok(authService.login(loginRequest.email(), loginRequest.password()));

    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout endpoint called");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }


    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid OtpEmailRequest request) {
        log.info("Send OTP endpoint called - email={}", request.email());
        otpService.sendOtp(request.email());
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@RequestBody @Valid OtpRequest request) {
        log.info("Verify OTP endpoint called - email={}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        otpService.verifyOtp(request.email(), request.otp());

        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User verified successfully - email={}, id={}", user.getEmail(), user.getId());

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(PublicUserDto.from(user), token, "Bearer", jwtService.getExpirationTime(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        log.info("Forgot password endpoint called - email={}", request.email());
        otpService.sendOtp(request.email(), "PASSWORD_RESET");
        return ResponseEntity.ok(Map.of("message", "Password reset OTP sent successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        log.info("Reset password endpoint called - email={}", request.email());
        otpService.verifyOtp(request.email(), request.otp(), "PASSWORD_RESET");
        userService.resetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}

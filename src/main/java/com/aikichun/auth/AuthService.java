package com.aikichun.auth;

import com.aikichun.auth.dto.AuthResponse;
import com.aikichun.auth.dto.PublicUserDto;
import com.aikichun.auth.dto.RegisterRequest;
import com.aikichun.exception.EmailAlreadyExistsException;
import com.aikichun.exception.FileStorageException;
import com.aikichun.exception.InvalidCredentialsException;
import com.aikichun.storage.FileStorageService;
import com.aikichun.user.entites.AccountStatus;
import com.aikichun.user.entites.Branch;
import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;
import com.aikichun.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt - email={}, branch={}", request.email(), request.branch());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Register failed - email already exists: {}", request.email());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        validatePassword(request.password());
        validatePhoneNumber("phoneNumber", request.phoneNumber());
        validatePhoneNumber("phoneNumberEmergency", request.phoneNumberEmergency());
        validateBranch(request.branch());

        if (Boolean.TRUE.equals(request.hasChronicConditionOrInjury())
                && (request.medicalNotes() == null || request.medicalNotes().isBlank())) {
            throw new IllegalArgumentException("Medical notes are required when chronic condition is true");
        }

        String profilePhotoPath = storeFile(request.profilePhoto(), "profile photo", request.email());
        String idPhotoPath = storeFile(request.idPhoto(), "id photo", request.email());

        User user = buildUser(request, profilePhotoPath, idPhotoPath);
        userRepository.save(user);

        log.info("Register success - id={}, email={}, branch={}", user.getId(), user.getEmail(), user.getBranch());

        return new AuthResponse(PublicUserDto.from(user), null, null, null);
    }

    public AuthResponse login(String email, String password) {
        log.info("Login attempt - email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", email);
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!user.getEmailVerified()) {
            log.warn("Login failed - email not verified: {}", email);
            throw new InvalidCredentialsException("Please verify your email first");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            log.warn("Login failed - account not active: email={}, status={}", email, user.getAccountStatus());
            throw new InvalidCredentialsException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed - password mismatch: email={}", email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("Login success - id={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(PublicUserDto.from(user), token, "Bearer", jwtService.getExpirationTime(token));
    }

    public void logout(String token) {
        log.info("Logout - token prefix={}", token.substring(0, Math.min(token.length(), 20)));
    }

    private User buildUser(RegisterRequest request, String profilePhotoPath, String idPhotoPath) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDateOfBirth(request.dateOfBirth());
        user.setProfession(request.profession());
        user.setBranch(Branch.valueOf(request.branch()));
        user.setPhoneNumber(request.phoneNumber());
        user.setPhoneNumberEmergency(request.phoneNumberEmergency());
        user.setServicesOrExperience(request.servicesOrExperience());
        user.setProductsAndDiscounts(request.productsAndDiscounts());
        user.setMartialArtsExperience(request.martialArtsExperience());
        user.setHasChronicConditionOrInjury(request.hasChronicConditionOrInjury());
        user.setMedicalNotes(request.medicalNotes());
        user.setSubscriptionStartMonth(request.subscriptionStartMonth());
        user.setSubscriptionStartYear(request.subscriptionStartYear());
        user.setProfilePhotoPath(profilePhotoPath);
        user.setIdPhotoPath(idPhotoPath);
        user.setRole(Role.MEMBER);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        return user;
    }

    private String storeFile(MultipartFile file, String fileType, String email) {
        try {
            String path = fileStorageService.storeFile(file, fileType);
            log.debug("File stored - type={}, email={}, path={}", fileType, email, path);
            return path;
        } catch (Exception e) {
            log.error("File upload failed - type={}, email={}", fileType, email, e);
            throw new FileStorageException("Failed to upload " + fileType + ": " + e.getMessage());
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    private void validatePhoneNumber(String fieldName, String phone) {
        if (!phone.matches("^\\+[1-9]\\d{6,14}$")) {
            throw new IllegalArgumentException(fieldName + " must be in E.164 format (e.g., +201234567890)");
        }
    }

    private void validateBranch(String branch) {
        try {
            Branch.valueOf(branch);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid branch. Must be one of: " + java.util.Arrays.stream(Branch.values())
                            .map(Enum::name)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("")
            );
        }
    }
}

package com.aikichun.user;

import com.aikichun.exception.InvalidCredentialsException;
import com.aikichun.storage.FileStorageService;
import com.aikichun.user.dto.UpdateProfileRequest;
import com.aikichun.user.dto.UserDto;
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
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        log.info("Profile requested for email={}", email);
        return UserDto.from(user);
    }

    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.profession() != null) user.setProfession(request.profession());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.phoneNumberEmergency() != null) user.setPhoneNumberEmergency(request.phoneNumberEmergency());
        if (request.medicalNotes() != null) user.setMedicalNotes(request.medicalNotes());

        userRepository.save(user);
        log.info("Profile updated for email={}", email);
        return UserDto.from(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("New password must contain at least one uppercase letter");
        }
        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("New password must contain at least one digit");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for email={}", email);
    }

    @Transactional
    public String updateProfilePhoto(String email, MultipartFile photo) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String path = fileStorageService.storeFile(photo, "profile photo");
        user.setProfilePhotoPath(path);
        userRepository.save(user);
        log.info("Profile photo updated for email={}", email);
        return path;
    }
}

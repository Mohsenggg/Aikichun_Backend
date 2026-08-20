package com.aikichun.user;

import com.aikichun.user.dto.ChangePasswordRequest;
import com.aikichun.user.dto.UpdateProfileRequest;
import com.aikichun.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PutMapping("/photo")
    public ResponseEntity<Map<String, String>> updatePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam MultipartFile profilePhoto) {
        String path = userService.updateProfilePhoto(userDetails.getUsername(), profilePhoto);
        return ResponseEntity.ok(Map.of("message", "Photo updated successfully", "path", path));
    }
}

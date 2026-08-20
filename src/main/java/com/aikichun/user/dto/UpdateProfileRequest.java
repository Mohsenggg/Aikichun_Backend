package com.aikichun.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        String profession,
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must be E.164 format (e.g., +201234567890)")
        String phoneNumber,
        @NotBlank(message = "Emergency phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Emergency phone must be E.164 format (e.g., +201234567890)")
        String phoneNumberEmergency,
        String medicalNotes
) {
}

package com.aikichun.auth.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        String password,
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,
        String profession,

        @NotBlank(message = "Branch is required") String branch,
        @NotBlank(message = "Phone number is required") String phoneNumber,
        @NotBlank(message = "Emergency phone number is required") String phoneNumberEmergency,
        String servicesOrExperience,
        String productsAndDiscounts,
        String martialArtsExperience,

        @NotNull(message = "Chronic condition flag is required")
        Boolean hasChronicConditionOrInjury,
        String medicalNotes,
        @NotNull(message = "Subscription start month is required") @Min(1) @Max(12)
        Integer subscriptionStartMonth,

        @NotNull(message = "Subscription start year is required") @Min(2000)
        Integer subscriptionStartYear,
        MultipartFile profilePhoto,
        MultipartFile idPhoto
) {
}

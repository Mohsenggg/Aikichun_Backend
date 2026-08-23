package com.aikichun.user.dto;

public record UpdateProfileRequest(
        String fullName,
        String profession,
        String phoneNumber,
        String phoneNumberEmergency,
        String medicalNotes
) {
}

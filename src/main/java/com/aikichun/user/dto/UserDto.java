package com.aikichun.user.dto;

import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(UUID id,
                      String fullName,
                      String email,
                      LocalDate dateOfBirth,
                      String profession,
                      String branch,
                      String phoneNumber,
                      String phoneNumberEmergency,
                      String servicesOrExperience,
                      String productsAndDiscounts,
                      String martialArtsExperience,
                      Boolean hasChronicConditionOrInjury,
                      String medicalNotes,
                      Integer subscriptionStartMonth,
                      Integer subscriptionStartYear,
                      Role role,
                      String profilePhotoPath) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getProfession(),
                user.getBranch().name(),
                user.getPhoneNumber(),
                user.getPhoneNumberEmergency(),
                user.getServicesOrExperience(),
                user.getProductsAndDiscounts(),
                user.getMartialArtsExperience(),
                user.getHasChronicConditionOrInjury(),
                user.getMedicalNotes(),
                user.getSubscriptionStartMonth(),
                user.getSubscriptionStartYear(),
                user.getRole(),
                user.getProfilePhotoPath()
        );
    }
}

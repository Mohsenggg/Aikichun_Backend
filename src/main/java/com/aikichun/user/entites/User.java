package com.aikichun.user.entites;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    private String profession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Branch branch;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "phone_number_emergency", nullable = false)
    private String phoneNumberEmergency;

    @Column(name = "services_or_experience")
    private String servicesOrExperience;

    @Column(name = "products_and_discounts")
    private String productsAndDiscounts;

    @Column(name = "martial_arts_experience")
    private String martialArtsExperience;

    @Column(name = "has_chronic_condition_or_injury", nullable = false)
    private Boolean hasChronicConditionOrInjury = false;

    @Column(name = "medical_notes")
    private String medicalNotes;

    @Column(name = "subscription_start_month", nullable = false)
    private Integer subscriptionStartMonth;

    @Column(name = "subscription_start_year", nullable = false)
    private Integer subscriptionStartYear;

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    @Column(name = "id_photo_path")
    private String idPhotoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}

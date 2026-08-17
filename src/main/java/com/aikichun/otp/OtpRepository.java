package com.aikichun.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmailAndOtpCode(String email, String otpCode);

    void deleteByEmail(String email);
}

package com.aikichun.auth;

import com.aikichun.exception.OtpSendException;
import com.aikichun.exception.OtpVerifiedException;
import com.aikichun.otp.Otp;
import com.aikichun.otp.OtpRepository;
import com.aikichun.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    @Value("${aikichun.otp.expiry-minutes:5}")
    private int otpExpiryTime;

    @Transactional
    public void sendOtp(String email) {
        log.info("OTP generation requested for email: {}", email);

        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("No user found with this email");
        }

        try {
            // Generate 6-digit OTP code
            String otpCode = String.format("%06d", new Random().nextInt(999999));
            // Hash it (SHA-256)
            String hashedOtp = hashOtp(otpCode);

            // Delete any existing OTPs for this email
            otpRepository.deleteByEmail(email);
            log.debug("Deleted existing OTPs for email: {}", email);

            // Save the hashed OTP in the database
            Otp otp = Otp.builder()
                    .email(email)
                    .otpCode(hashedOtp)
                    .expiresAt(Instant.now().plus(otpExpiryTime, ChronoUnit.MINUTES))
                    .build();
            otpRepository.save(otp);
            log.info("OTP generated and saved for email: {}, expires at: {}", email, otp.getExpiresAt());

            try {

                // Send the OTP via email
                emailService.sendOtpEmail(email, otpCode);
            } catch (Exception e) {
                log.error("Failed to send OTP email to: {}", email, e);
                throw new OtpSendException("Failed to send OTP email. Please try again later.");
            }

        } catch (Exception e) {
            log.error("Failed to send OTP for email: {}", email, e);
            throw e; // Re-throw so controller can handle it
        }
    }

    public boolean verifyOtp(String email, String otpCode) {
        log.info("Verifying OTP for email: {}", email);
        String hashedOtp = hashOtp(otpCode);

        Otp otp = otpRepository.findByEmailAndOtpCode(email, hashedOtp)
                .orElseThrow(() -> {
                    log.warn("OTP verification failed - invalid OTP for email: {}", email);
                    return new OtpVerifiedException("Invalid OTP");
                });

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            log.warn("OTP has expired for email: {}", email);
            throw new OtpVerifiedException("OTP has expired");
        }
        if (otp.getConsumed()) {
            log.warn("OTP has already been consumed for email: {}", email);
            throw new OtpVerifiedException("OTP has already been consumed");
        }

        otp.setConsumed(true);
        otpRepository.save(otp);
        log.info("OTP verified successfully for email: {}", email);
        return true;
    }

    private String hashOtp(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 not available", e);
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

}

package com.aikichun.auth;

import com.aikichun.otp.Otp;
import com.aikichun.otp.OtpRepository;
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

    @Value("${aikichun.otp.expiry-minutes:5}")
    private int otpExpiryTime;

    /*
        public void sendOtp(String email) {
            log.info("OTP generation requested for email: {}", email);

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

            // Send the OTP via email
            emailService.sendOtpEmail(email, otpCode);
        }
    */
    @Transactional
    public void sendOtp(String email) {
        log.info("OTP generation requested for email: {}", email);

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

            // Send the OTP via email
            emailService.sendOtpEmail(email, otpCode);
            log.info("OTP email sent successfully to: {}", email);

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
                    return new IllegalArgumentException("Invalid OTP");
                });

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            log.warn("OTP has expired for email: {}", email);
            throw new IllegalArgumentException("OTP has expired");
        }
        if (otp.getConsumed()) {
            log.warn("OTP has already been consumed for email: {}", email);
            throw new IllegalArgumentException("OTP has already been consumed");
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

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
        sendOtp(email, "EMAIL_VERIFICATION");
    }

    @Transactional
    public void sendOtp(String email, String type) {
        log.info("OTP generation requested for email: {}, type: {}", email, type);

        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("No user found with this email");
        }

        try {
            String otpCode = String.format("%06d", new Random().nextInt(999999));
            String hashedOtp = hashOtp(otpCode);

            otpRepository.deleteByEmailAndType(email, type);
            log.debug("Deleted existing OTPs for email: {}, type: {}", email, type);

            Otp otp = Otp.builder()
                    .email(email)
                    .otpCode(hashedOtp)
                    .expiresAt(Instant.now().plus(otpExpiryTime, ChronoUnit.MINUTES))
                    .type(type)
                    .build();
            otpRepository.save(otp);
            log.info("OTP generated and saved for email: {}, type: {}, expires at: {}", email, type, otp.getExpiresAt());

            try {
                emailService.sendOtpEmail(email, otpCode);
            } catch (Exception e) {
                log.error("Failed to send OTP email to: {}", email, e);
                throw new OtpSendException("Failed to send OTP email. Please try again later.");
            }

        } catch (Exception e) {
            log.error("Failed to send OTP for email: {}, type: {}", email, type, e);
            throw e;
        }
    }

    public boolean verifyOtp(String email, String otpCode) {
        return verifyOtp(email, otpCode, "EMAIL_VERIFICATION");
    }

    public boolean verifyOtp(String email, String otpCode, String type) {
        log.info("Verifying OTP for email: {}, type: {}", email, type);
        String hashedOtp = hashOtp(otpCode);

        Otp otp = otpRepository.findByEmailAndOtpCodeAndType(email, hashedOtp, type)
                .orElseThrow(() -> {
                    log.warn("OTP verification failed - invalid OTP for email: {}, type: {}", email, type);
                    return new OtpVerifiedException("Invalid OTP");
                });

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            log.warn("OTP has expired for email: {}, type: {}", email, type);
            throw new OtpVerifiedException("OTP has expired");
        }
        if (otp.getConsumed()) {
            log.warn("OTP has already been consumed for email: {}, type: {}", email, type);
            throw new OtpVerifiedException("OTP has already been consumed");
        }

        otp.setConsumed(true);
        otpRepository.save(otp);
        log.info("OTP verified successfully for email: {}, type: {}", email, type);
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

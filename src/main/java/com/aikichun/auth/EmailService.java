package com.aikichun.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${aikichun.otp.email.from-email}")
    private String fromEmail;
    @Value("${aikichun.otp.email.from-name}")
    private String fromName;


    public void sendOtpEmail(String toEmail, String otpCode) {
        log.info("Sending OTP email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Aikichun OTP Code 🥷");
            message.setText("Your OTP code is: " + otpCode + "\n\nThis code expires in 5 minutes." +
                    "\n\nIf you did not request this code, please ignore this email.");
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

}

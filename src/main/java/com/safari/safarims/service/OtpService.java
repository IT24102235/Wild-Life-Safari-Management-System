package com.safari.safarims.service;

import com.safari.safarims.entity.Otp;
import com.safari.safarims.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String OTP_TYPE_SIGNUP = "SIGNUP";
    private static final String OTP_TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public String generateAndSendSignupOtp(String email) {
        return generateAndSendOtp(email, OTP_TYPE_SIGNUP, "Account Verification");
    }

    @Transactional
    public String generateAndSendPasswordResetOtp(String email) {
        return generateAndSendOtp(email, OTP_TYPE_PASSWORD_RESET, "Password Reset");
    }

    private String generateAndSendOtp(String email, String type, String purpose) {
        // Generate 6-digit OTP
        String otp = generateOtp();

        // Hash the OTP before storing
        String hashedOtp = passwordEncoder.encode(otp);

        // Create OTP entity
        Otp otpEntity = Otp.builder()
            .email(email)
            .otpHash(hashedOtp)
            .type(type)
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();

        otpRepository.save(otpEntity);

        // Send OTP via email
        emailService.sendOtp(email, otp, purpose);

        log.info("OTP generated and sent for email: {} (type: {})", email, type);
        return otp; // Return plain OTP for testing/logging purposes
    }

    @Transactional
    public boolean verifySignupOtp(String email, String otp) {
        return verifyOtp(email, otp, OTP_TYPE_SIGNUP);
    }

    @Transactional
    public boolean verifyPasswordResetOtp(String email, String otp) {
        return verifyOtp(email, otp, OTP_TYPE_PASSWORD_RESET);
    }

    private boolean verifyOtp(String email, String otp, String type) {
        Optional<Otp> otpEntityOpt = otpRepository.findByEmailAndTypeAndUsedAtIsNullAndExpiresAtAfter(
            email, type, LocalDateTime.now());

        if (otpEntityOpt.isEmpty()) {
            log.warn("No valid OTP found for email: {} (type: {})", email, type);
            return false;
        }

        Otp otpEntity = otpEntityOpt.get();

        // Check if max attempts exceeded
        if (otpEntity.getAttempts() >= otpEntity.getMaxAttempts()) {
            log.warn("Max OTP attempts exceeded for email: {} (type: {})", email, type);
            return false;
        }

        // Increment attempts
        otpEntity.setAttempts(otpEntity.getAttempts() + 1);

        // Verify OTP
        if (passwordEncoder.matches(otp, otpEntity.getOtpHash())) {
            otpEntity.setUsedAt(LocalDateTime.now());
            otpRepository.save(otpEntity);
            log.info("OTP verified successfully for email: {} (type: {})", email, type);
            return true;
        } else {
            otpRepository.save(otpEntity);
            log.warn("Invalid OTP provided for email: {} (type: {})", email, type);
            return false;
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired OTPs");
    }
}

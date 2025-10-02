package com.safari.safarims.service;

import com.safari.safarims.dto.auth.*;
import com.safari.safarims.entity.User;
import com.safari.safarims.entity.Tourist;
import com.safari.safarims.common.enums.UserRole;
import com.safari.safarims.repository.TouristRepository;
import com.safari.safarims.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final OtpService otpService;
    private final TouristRepository touristRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse registerTourist(RegisterRequest request) {
        // Check if user already exists
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user account
        User user = userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            UserRole.TOURIST,
            "SYSTEM"
        );

        // Create tourist profile
        Tourist tourist = Tourist.builder()
            .user(user)
            .fullName(request.getFullName())
            .build();

        touristRepository.save(tourist);

        // Generate and send OTP
        String otp = otpService.generateAndSendSignupOtp(request.getEmail());

        log.info("Tourist registered successfully: {}", request.getUsername());

        return AuthResponse.builder()
            .message("Registration successful. Please check your email for OTP verification.")
            .email(request.getEmail())
            .username(request.getUsername())
            .build();
    }

    @Transactional
    public AuthResponse verifySignup(VerifyOtpRequest request) {
        // Verify OTP
        boolean isValidOtp = otpService.verifySignupOtp(request.getEmail(), request.getOtp());

        if (!isValidOtp) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Mark email as verified
        userService.verifyEmail(request.getEmail());

        // Get user for token generation
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name());

        log.info("Email verified and user logged in: {}", user.getUsername());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtExpiration)
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole().name())
            .message("Email verification successful. You are now logged in.")
            .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by username or email
        User user = userService.findByUsernameOrEmail(request.getUsernameOrEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Check if user is enabled and not locked
        if (!user.getEnabled()) {
            throw new RuntimeException("Account is disabled");
        }

        if (user.getLocked()) {
            throw new RuntimeException("Account is locked");
        }

        // For tourists, check if email is verified
        if (user.getRole() == UserRole.TOURIST && !user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Update last login time
        userService.updateLastLogin(user.getUsername());

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name());

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtExpiration)
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole().name())
            .message("Login successful")
            .build();
    }

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        // Check if user exists
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Generate and send OTP
        String otp = otpService.generateAndSendPasswordResetOtp(request.getEmail());

        log.info("Password reset OTP sent to: {}", request.getEmail());

        return AuthResponse.builder()
            .message("Password reset OTP sent to your email")
            .email(request.getEmail())
            .build();
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        // Verify OTP
        boolean isValidOtp = otpService.verifyPasswordResetOtp(request.getEmail(), request.getOtp());

        if (!isValidOtp) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Update password
        userService.updatePassword(request.getEmail(), request.getNewPassword());

        log.info("Password reset successfully for: {}", request.getEmail());

        return AuthResponse.builder()
            .message("Password reset successful. Please login with your new password.")
            .email(request.getEmail())
            .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            String role = jwtUtil.extractRole(refreshToken);

            if (jwtUtil.validateToken(refreshToken, username)) {
                String newAccessToken = jwtUtil.generateToken(username, role);
                String newRefreshToken = jwtUtil.generateRefreshToken(username, role);

                return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtExpiration)
                    .username(username)
                    .role(role)
                    .message("Token refreshed successfully")
                    .build();
            } else {
                throw new RuntimeException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }
}

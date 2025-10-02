package com.safari.safarims.controller;

import com.safari.safarims.dto.auth.*;
import com.safari.safarims.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/tourist")
    @Operation(summary = "Register new tourist", description = "Register a new tourist account and send email verification OTP")
    public ResponseEntity<AuthResponse> registerTourist(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerTourist(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Tourist registration failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Registration failed: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verify-signup")
    @Operation(summary = "Verify signup OTP", description = "Verify email OTP sent during tourist registration")
    public ResponseEntity<AuthResponse> verifySignup(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            AuthResponse response = authService.verifySignup(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OTP verification failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Verification failed: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login for all user types using username/email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Login failed: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset OTP to user email")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            AuthResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Forgot password failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Failed to send reset OTP: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using OTP verification")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            AuthResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Password reset failed: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                AuthResponse errorResponse = AuthResponse.builder()
                    .message("Invalid authorization header")
                    .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String refreshToken = authHeader.substring(7);
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Token refresh failed: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

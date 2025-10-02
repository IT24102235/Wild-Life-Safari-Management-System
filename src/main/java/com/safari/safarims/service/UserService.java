package com.safari.safarims.service;

import com.safari.safarims.entity.User;
import com.safari.safarims.common.enums.UserRole;
import com.safari.safarims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(String username, String email, String password, UserRole role, String createdBy) {
        if (existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        if (existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
            .username(username)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(role)
            .enabled(true)
            .locked(false)
            .emailVerified(role != UserRole.TOURIST) // Only tourists need email verification
            .build();

        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);

        User savedUser = userRepository.save(user);
        log.info("User created: {} with role: {}", username, role);
        return savedUser;
    }

    @Transactional
    public void verifyEmail(String email) {
        User user = findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", email);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user: {}", email);
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> findAllEnabled() {
        return userRepository.findByEnabledTrue();
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled: {}", user.getUsername());
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled: {}", user.getUsername());
    }

    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setLocked(true);
        userRepository.save(user);
        log.info("User locked: {}", user.getUsername());
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setLocked(false);
        userRepository.save(user);
        log.info("User unlocked: {}", user.getUsername());
    }
}

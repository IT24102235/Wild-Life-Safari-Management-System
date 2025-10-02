package com.safari.safarims.service;

import com.safari.safarims.dto.auth.AuthResponse;
import com.safari.safarims.entity.*;
import com.safari.safarims.common.enums.UserRole;
import com.safari.safarims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffManagementService {

    private final UserService userService;
    private final DriverRepository driverRepository;
    private final GuideRepository guideRepository;
    private final MechanicRepository mechanicRepository;
    private final LanguageRepository languageRepository;

    // Admin creates staff accounts
    @Transactional
    public AuthResponse createStaffAccount(String username, String email, String password,
                                         String fullName, String phone, UserRole role) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Create user account
        User user = userService.createUser(username, email, password, role, currentUsername);

        log.info("Staff account created: {} with role: {} by {}", username, role, currentUsername);

        return AuthResponse.builder()
            .message("Staff account created successfully")
            .username(username)
            .email(email)
            .role(role.name())
            .build();
    }

    // Crew Manager creates Driver accounts
    @Transactional
    public AuthResponse createDriver(String username, String email, String password,
                                   String fullName, String phone, String licenseNo) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Check if license number already exists
        if (driverRepository.existsByLicenseNo(licenseNo)) {
            throw new RuntimeException("Driver with license number " + licenseNo + " already exists");
        }

        // Create user account
        User user = userService.createUser(username, email, password, UserRole.DRIVER, currentUsername);

        // Create driver profile
        Driver driver = Driver.builder()
            .id(user.getId())
            .user(user)
            .fullName(fullName)
            .phone(phone)
            .licenseNo(licenseNo)
            .build();

        driver.setCreatedBy(currentUsername);
        driver.setUpdatedBy(currentUsername);

        driverRepository.save(driver);

        log.info("Driver created: {} by {}", fullName, currentUsername);

        return AuthResponse.builder()
            .message("Driver account created successfully")
            .username(username)
            .email(email)
            .role(UserRole.DRIVER.name())
            .build();
    }

    // Crew Manager creates Guide accounts
    @Transactional
    public AuthResponse createGuide(String username, String email, String password,
                                  String fullName, String phone, Set<String> languageCodes) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Create user account
        User user = userService.createUser(username, email, password, UserRole.GUIDE, currentUsername);

        // Get languages
        Set<Language> languages = Set.copyOf(languageRepository.findAll().stream()
            .filter(lang -> languageCodes.contains(lang.getIsoCode()))
            .toList());

        // Create guide profile
        Guide guide = Guide.builder()
            .id(user.getId())
            .user(user)
            .fullName(fullName)
            .phone(phone)
            .languages(languages)
            .build();

        guide.setCreatedBy(currentUsername);
        guide.setUpdatedBy(currentUsername);

        guideRepository.save(guide);

        log.info("Guide created: {} by {}", fullName, currentUsername);

        return AuthResponse.builder()
            .message("Guide account created successfully")
            .username(username)
            .email(email)
            .role(UserRole.GUIDE.name())
            .build();
    }

    // Maintenance Officer creates Mechanic accounts
    @Transactional
    public AuthResponse createMechanic(String username, String email, String password,
                                     String fullName, String phone, String skills) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // Create user account
        User user = userService.createUser(username, email, password, UserRole.MECHANIC, currentUsername);

        // Create mechanic profile
        Mechanic mechanic = Mechanic.builder()
            .id(user.getId())
            .user(user)
            .fullName(fullName)
            .phone(phone)
            .skills(skills)
            .build();

        mechanic.setCreatedBy(currentUsername);
        mechanic.setUpdatedBy(currentUsername);

        mechanicRepository.save(mechanic);

        log.info("Mechanic created: {} by {}", fullName, currentUsername);

        return AuthResponse.builder()
            .message("Mechanic account created successfully")
            .username(username)
            .email(email)
            .role(UserRole.MECHANIC.name())
            .build();
    }

    // Delete staff accounts
    @Transactional
    public void deleteDriver(Long driverId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Check if driver has active allocations
        // This would be checked by the allocation service

        driverRepository.delete(driver);
        log.info("Driver deleted: {} by {}", driver.getFullName(), currentUsername);
    }

    @Transactional
    public void deleteGuide(Long guideId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Guide guide = guideRepository.findById(guideId)
            .orElseThrow(() -> new RuntimeException("Guide not found"));

        guideRepository.delete(guide);
        log.info("Guide deleted: {} by {}", guide.getFullName(), currentUsername);
    }

    @Transactional
    public void deleteMechanic(Long mechanicId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
            .orElseThrow(() -> new RuntimeException("Mechanic not found"));

        mechanicRepository.delete(mechanic);
        log.info("Mechanic deleted: {} by {}", mechanic.getFullName(), currentUsername);
    }

    // Get staff lists
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public List<User> getStaffByRole(UserRole role) {
        return userService.findByRole(role);
    }
}

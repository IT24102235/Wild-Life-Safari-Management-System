package com.safari.safarims.controller;

import com.safari.safarims.dto.auth.AuthResponse;
import com.safari.safarims.entity.*;
import com.safari.safarims.common.enums.UserRole;
import com.safari.safarims.service.StaffManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Staff Management", description = "Admin endpoints for staff management")
public class AdminController {

    private final StaffManagementService staffManagementService;

    @PostMapping("/create/{role}")
    @Operation(summary = "Create staff account", description = "Create staff accounts (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createStaffAccount(@PathVariable UserRole role,
                                                          @RequestParam String username,
                                                          @RequestParam String email,
                                                          @RequestParam String password,
                                                          @RequestParam String fullName,
                                                          @RequestParam String phone) {
        try {
            AuthResponse response = staffManagementService.createStaffAccount(
                username, email, password, fullName, phone, role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating staff account: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Failed to create staff account: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/staff/{role}")
    @Operation(summary = "Get staff by role", description = "Retrieve staff members by role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getStaffByRole(@PathVariable UserRole role) {
        List<User> staff = staffManagementService.getStaffByRole(role);
        return ResponseEntity.ok(staff);
    }
}

@RestController
@RequestMapping("/api/v1/crew")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Management", description = "Tour & Crew Manager endpoints")
class CrewController {

    private final StaffManagementService staffManagementService;

    @PostMapping("/drivers")
    @Operation(summary = "Create driver", description = "Create new driver account")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createDriver(@RequestParam String username,
                                                    @RequestParam String email,
                                                    @RequestParam String password,
                                                    @RequestParam String fullName,
                                                    @RequestParam String phone,
                                                    @RequestParam String licenseNo) {
        try {
            AuthResponse response = staffManagementService.createDriver(
                username, email, password, fullName, phone, licenseNo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating driver: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Failed to create driver: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/guides")
    @Operation(summary = "Create guide", description = "Create new guide account")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createGuide(@RequestParam String username,
                                                   @RequestParam String email,
                                                   @RequestParam String password,
                                                   @RequestParam String fullName,
                                                   @RequestParam String phone,
                                                   @RequestParam Set<String> languageCodes) {
        try {
            AuthResponse response = staffManagementService.createGuide(
                username, email, password, fullName, phone, languageCodes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating guide: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Failed to create guide: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/drivers/{id}")
    @Operation(summary = "Delete driver", description = "Delete driver account")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        try {
            staffManagementService.deleteDriver(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting driver {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/guides/{id}")
    @Operation(summary = "Delete guide", description = "Delete guide account")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuide(@PathVariable Long id) {
        try {
            staffManagementService.deleteGuide(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting guide {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/drivers")
    @Operation(summary = "Get all drivers", description = "Retrieve all drivers")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = staffManagementService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/guides")
    @Operation(summary = "Get all guides", description = "Retrieve all guides")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<Guide>> getAllGuides() {
        List<Guide> guides = staffManagementService.getAllGuides();
        return ResponseEntity.ok(guides);
    }
}

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maintenance Management", description = "Maintenance Officer endpoints")
class MaintenanceController {

    private final StaffManagementService staffManagementService;

    @PostMapping("/mechanics")
    @Operation(summary = "Create mechanic", description = "Create new mechanic account")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> createMechanic(@RequestParam String username,
                                                      @RequestParam String email,
                                                      @RequestParam String password,
                                                      @RequestParam String fullName,
                                                      @RequestParam String phone,
                                                      @RequestParam String skills) {
        try {
            AuthResponse response = staffManagementService.createMechanic(
                username, email, password, fullName, phone, skills);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating mechanic: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                .message("Failed to create mechanic: " + e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/mechanics/{id}")
    @Operation(summary = "Delete mechanic", description = "Delete mechanic account")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMechanic(@PathVariable Long id) {
        try {
            staffManagementService.deleteMechanic(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting mechanic {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mechanics")
    @Operation(summary = "Get all mechanics", description = "Retrieve all mechanics")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<Mechanic>> getAllMechanics() {
        List<Mechanic> mechanics = staffManagementService.getAllMechanics();
        return ResponseEntity.ok(mechanics);
    }
}

package com.safari.safarims.controller;

import com.safari.safarims.dto.tourpackage.TourPackageRequest;
import com.safari.safarims.dto.tourpackage.TourPackageResponse;
import com.safari.safarims.service.TourPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tour Packages", description = "Tour package management endpoints")
public class TourPackageController {

    private final TourPackageService tourPackageService;

    @GetMapping
    @Operation(summary = "Get all active packages", description = "Retrieve all active tour packages (public endpoint)")
    public ResponseEntity<List<TourPackageResponse>> getAllActivePackages() {
        List<TourPackageResponse> packages = tourPackageService.getAllActivePackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all packages", description = "Retrieve all tour packages including inactive ones")
    @PreAuthorize("hasRole('TOUR_PACKAGE_BUILDER') or hasRole('ADMIN')")
    public ResponseEntity<List<TourPackageResponse>> getAllPackages() {
        List<TourPackageResponse> packages = tourPackageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package by ID", description = "Retrieve a specific tour package by ID")
    public ResponseEntity<TourPackageResponse> getPackageById(@PathVariable Long id) {
        try {
            TourPackageResponse tourPackage = tourPackageService.getPackageById(id);
            return ResponseEntity.ok(tourPackage);
        } catch (Exception e) {
            log.error("Error fetching package {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new package", description = "Create a new tour package")
    @PreAuthorize("hasRole('TOUR_PACKAGE_BUILDER') or hasRole('ADMIN')")
    public ResponseEntity<TourPackageResponse> createPackage(@Valid @RequestBody TourPackageRequest request) {
        try {
            TourPackageResponse created = tourPackageService.createPackage(request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating package: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update package", description = "Update an existing tour package")
    @PreAuthorize("hasRole('TOUR_PACKAGE_BUILDER') or hasRole('ADMIN')")
    public ResponseEntity<TourPackageResponse> updatePackage(@PathVariable Long id,
                                                            @Valid @RequestBody TourPackageRequest request) {
        try {
            TourPackageResponse updated = tourPackageService.updatePackage(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating package {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete package", description = "Deactivate a tour package (soft delete)")
    @PreAuthorize("hasRole('TOUR_PACKAGE_BUILDER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        try {
            tourPackageService.deletePackage(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting package {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search packages", description = "Search tour packages by name")
    public ResponseEntity<List<TourPackageResponse>> searchPackages(@RequestParam String name) {
        List<TourPackageResponse> packages = tourPackageService.searchPackages(name);
        return ResponseEntity.ok(packages);
    }
}

package com.safari.safarims.controller;

import com.safari.safarims.dto.jeep.JeepRequest;
import com.safari.safarims.dto.jeep.JeepResponse;
import com.safari.safarims.common.enums.JeepStatus;
import com.safari.safarims.service.JeepService;
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
@RequestMapping("/api/v1/jeeps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jeeps", description = "Vehicle/Jeep management endpoints")
public class JeepController {

    private final JeepService jeepService;

    @GetMapping
    @Operation(summary = "Get all jeeps", description = "Retrieve all jeeps in the system")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<JeepResponse>> getAllJeeps() {
        List<JeepResponse> jeeps = jeepService.getAllJeeps();
        return ResponseEntity.ok(jeeps);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available jeeps", description = "Retrieve only available jeeps for allocation")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<JeepResponse>> getAvailableJeeps() {
        List<JeepResponse> jeeps = jeepService.getAvailableJeeps();
        return ResponseEntity.ok(jeeps);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get jeep by ID", description = "Retrieve specific jeep details")
    public ResponseEntity<JeepResponse> getJeepById(@PathVariable Long id) {
        try {
            JeepResponse jeep = jeepService.getJeepById(id);
            return ResponseEntity.ok(jeep);
        } catch (Exception e) {
            log.error("Error fetching jeep {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create jeep", description = "Add a new jeep to the fleet")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<JeepResponse> createJeep(@Valid @RequestBody JeepRequest request) {
        try {
            JeepResponse jeep = jeepService.createJeep(request);
            return ResponseEntity.ok(jeep);
        } catch (Exception e) {
            log.error("Error creating jeep: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update jeep", description = "Update existing jeep details")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<JeepResponse> updateJeep(@PathVariable Long id,
                                                  @Valid @RequestBody JeepRequest request) {
        try {
            JeepResponse jeep = jeepService.updateJeep(id, request);
            return ResponseEntity.ok(jeep);
        } catch (Exception e) {
            log.error("Error updating jeep {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete jeep", description = "Remove jeep from the fleet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJeep(@PathVariable Long id) {
        try {
            jeepService.deleteJeep(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting jeep {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/default-driver")
    @Operation(summary = "Set default driver", description = "Assign default driver to jeep")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> setDefaultDriver(@PathVariable Long id, @RequestParam Long driverId) {
        try {
            jeepService.setDefaultDriver(id, driverId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting default driver for jeep {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update jeep status", description = "Update jeep availability status")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateJeepStatus(@PathVariable Long id, @RequestParam JeepStatus status) {
        try {
            jeepService.updateJeepStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating jeep {} status: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get jeeps by driver", description = "Retrieve jeeps assigned to specific driver")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<List<JeepResponse>> getJeepsByDriver(@PathVariable Long driverId) {
        List<JeepResponse> jeeps = jeepService.getJeepsByDriver(driverId);
        return ResponseEntity.ok(jeeps);
    }
}

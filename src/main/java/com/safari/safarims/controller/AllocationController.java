package com.safari.safarims.controller;

import com.safari.safarims.dto.allocation.AllocationRequest;
import com.safari.safarims.dto.allocation.AllocationResponse;
import com.safari.safarims.service.AllocationService;
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
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Allocations", description = "Allocation management endpoints")
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    @Operation(summary = "Create allocation", description = "Allocate driver, guide, and jeep to a booking")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AllocationResponse> createAllocation(@Valid @RequestBody AllocationRequest request) {
        try {
            AllocationResponse allocation = allocationService.createAllocation(request);
            return ResponseEntity.ok(allocation);
        } catch (Exception e) {
            log.error("Error creating allocation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update allocation", description = "Update existing allocation resources")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<AllocationResponse> updateAllocation(@PathVariable Long id,
                                                             @Valid @RequestBody AllocationRequest request) {
        try {
            AllocationResponse allocation = allocationService.updateAllocation(id, request);
            return ResponseEntity.ok(allocation);
        } catch (Exception e) {
            log.error("Error updating allocation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel allocation", description = "Cancel allocation and release resources")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelAllocation(@PathVariable Long id) {
        try {
            allocationService.cancelAllocation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling allocation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all allocations", description = "Retrieve all allocations")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getAllAllocations() {
        List<AllocationResponse> allocations = allocationService.getAllAllocations();
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active allocations", description = "Retrieve only active allocations")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('BOOKING_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getActiveAllocations() {
        List<AllocationResponse> allocations = allocationService.getActiveAllocations();
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get allocation by ID", description = "Retrieve specific allocation details")
    public ResponseEntity<AllocationResponse> getAllocationById(@PathVariable Long id) {
        try {
            AllocationResponse allocation = allocationService.getAllocationById(id);
            return ResponseEntity.ok(allocation);
        } catch (Exception e) {
            log.error("Error fetching allocation {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get allocations by driver", description = "Retrieve allocations for specific driver")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getAllocationsByDriver(@PathVariable Long driverId) {
        List<AllocationResponse> allocations = allocationService.getAllocationsByDriver(driverId);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/guide/{guideId}")
    @Operation(summary = "Get allocations by guide", description = "Retrieve allocations for specific guide")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getAllocationsByGuide(@PathVariable Long guideId) {
        List<AllocationResponse> allocations = allocationService.getAllocationsByGuide(guideId);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/jeep/{jeepId}")
    @Operation(summary = "Get allocations by jeep", description = "Retrieve allocations for specific jeep")
    @PreAuthorize("hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<AllocationResponse>> getAllocationsByJeep(@PathVariable Long jeepId) {
        List<AllocationResponse> allocations = allocationService.getAllocationsByJeep(jeepId);
        return ResponseEntity.ok(allocations);
    }
}

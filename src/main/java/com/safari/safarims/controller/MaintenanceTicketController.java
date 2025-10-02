package com.safari.safarims.controller;

import com.safari.safarims.dto.ticket.MaintenanceTicketRequest;
import com.safari.safarims.dto.ticket.MaintenanceTicketResponse;
import com.safari.safarims.common.enums.TicketStatus;
import com.safari.safarims.service.MaintenanceTicketService;
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
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maintenance Tickets", description = "Maintenance ticket management endpoints")
public class MaintenanceTicketController {

    private final MaintenanceTicketService ticketService;

    @PostMapping
    @Operation(summary = "Create maintenance ticket", description = "File a new maintenance ticket for a vehicle")
    @PreAuthorize("hasRole('DRIVER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<MaintenanceTicketResponse> createTicket(@Valid @RequestBody MaintenanceTicketRequest request) {
        try {
            MaintenanceTicketResponse ticket = ticketService.createTicket(request);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("Error creating maintenance ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all tickets", description = "Retrieve all maintenance tickets")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTicketResponse>> getAllTickets() {
        List<MaintenanceTicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/open")
    @Operation(summary = "Get open tickets", description = "Retrieve all open/in-progress tickets ordered by severity")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('MECHANIC') or hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTicketResponse>> getOpenTickets() {
        List<MaintenanceTicketResponse> tickets = ticketService.getOpenTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/my-tickets")
    @Operation(summary = "Get my tickets", description = "Get tickets filed by current user")
    @PreAuthorize("hasRole('DRIVER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTicketResponse>> getMyTickets() {
        List<MaintenanceTicketResponse> tickets = ticketService.getMyTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieve specific ticket details")
    public ResponseEntity<MaintenanceTicketResponse> getTicketById(@PathVariable Long id) {
        try {
            MaintenanceTicketResponse ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("Error fetching ticket {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get tickets by vehicle", description = "Retrieve all tickets for specific vehicle")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('TOUR_CREW_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTicketResponse>> getTicketsByVehicle(@PathVariable Long vehicleId) {
        List<MaintenanceTicketResponse> tickets = ticketService.getTicketsByVehicle(vehicleId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/mechanic/{mechanicId}")
    @Operation(summary = "Get tickets by mechanic", description = "Retrieve tickets assigned to specific mechanic")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('MECHANIC') or hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTicketResponse>> getTicketsByMechanic(@PathVariable Long mechanicId) {
        List<MaintenanceTicketResponse> tickets = ticketService.getTicketsByMechanic(mechanicId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/{id}/assign/{mechanicId}")
    @Operation(summary = "Assign mechanic", description = "Assign a mechanic to a maintenance ticket")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('ADMIN')")
    public ResponseEntity<Void> assignMechanic(@PathVariable Long id, @PathVariable Long mechanicId) {
        try {
            ticketService.assignMechanic(id, mechanicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error assigning mechanic {} to ticket {}: {}", mechanicId, id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update ticket status", description = "Update ticket status and add resolution notes")
    @PreAuthorize("hasRole('MAINTENANCE_OFFICER') or hasRole('MECHANIC') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateTicketStatus(@PathVariable Long id,
                                                  @RequestParam TicketStatus status,
                                                  @RequestParam(required = false) String resolutionNotes) {
        try {
            ticketService.updateTicketStatus(id, status, resolutionNotes);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating ticket {} status: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

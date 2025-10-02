package com.safari.safarims.service;

import com.safari.safarims.dto.ticket.MaintenanceTicketRequest;
import com.safari.safarims.dto.ticket.MaintenanceTicketResponse;
import com.safari.safarims.entity.MaintenanceTicket;
import com.safari.safarims.entity.User;
import com.safari.safarims.entity.Jeep;
import com.safari.safarims.entity.Mechanic;
import com.safari.safarims.common.enums.TicketStatus;
import com.safari.safarims.common.enums.JeepStatus;
import com.safari.safarims.repository.MaintenanceTicketRepository;
import com.safari.safarims.repository.UserRepository;
import com.safari.safarims.repository.JeepRepository;
import com.safari.safarims.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceTicketService {

    private final MaintenanceTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final JeepRepository jeepRepository;
    private final MechanicRepository mechanicRepository;
    private final NotificationService notificationService;

    @Transactional
    public MaintenanceTicketResponse createTicket(MaintenanceTicketRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current user not found"));

        Jeep vehicle = jeepRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        MaintenanceTicket ticket = MaintenanceTicket.builder()
            .vehicle(vehicle)
            .filedByUser(currentUser)
            .severity(request.getSeverity())
            .title(request.getTitle())
            .description(request.getDescription())
            .status(TicketStatus.OPEN)
            .openedAt(LocalDateTime.now())
            .build();

        ticket.setCreatedBy(currentUsername);
        ticket.setUpdatedBy(currentUsername);

        MaintenanceTicket saved = ticketRepository.save(ticket);

        // Update vehicle status to UNDER_REPAIR if severity is HIGH or CRITICAL
        if (request.getSeverity() == com.safari.safarims.common.enums.TicketSeverity.HIGH ||
            request.getSeverity() == com.safari.safarims.common.enums.TicketSeverity.CRITICAL) {
            vehicle.setStatus(JeepStatus.UNDER_REPAIR);
            jeepRepository.save(vehicle);
        }

        // Notify maintenance officers
        notificationService.notifyMaintenanceOfficers("New Maintenance Ticket",
            "New " + request.getSeverity() + " ticket #" + saved.getId() + " filed for vehicle " + vehicle.getPlateNo());

        log.info("Maintenance ticket created: {} for vehicle: {} by {}",
            saved.getId(), vehicle.getPlateNo(), currentUsername);

        return mapToResponse(saved);
    }

    @Transactional
    public void assignMechanic(Long ticketId, Long mechanicId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
            .orElseThrow(() -> new RuntimeException("Mechanic not found"));

        ticket.setAssigneeMechanic(mechanic);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedBy(currentUsername);

        ticketRepository.save(ticket);

        // Notify mechanic
        notificationService.notifyUser(mechanic.getUser().getId(), "MAINTENANCE",
            "Ticket Assignment", "You have been assigned to maintenance ticket #" + ticket.getId());

        log.info("Mechanic {} assigned to ticket {} by {}",
            mechanic.getFullName(), ticketId, currentUsername);
    }

    @Transactional
    public void updateTicketStatus(Long ticketId, TicketStatus status, String resolutionNotes) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);
        ticket.setUpdatedBy(currentUsername);

        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            ticket.setResolutionNotes(resolutionNotes);
        }

        if (status == TicketStatus.CLOSED || status == TicketStatus.RESOLVED) {
            ticket.setClosedAt(LocalDateTime.now());

            // Update vehicle status back to AVAILABLE if resolved/closed
            Jeep vehicle = ticket.getVehicle();
            vehicle.setStatus(JeepStatus.AVAILABLE);
            jeepRepository.save(vehicle);
        }

        ticketRepository.save(ticket);

        // Notify relevant parties
        if (ticket.getAssigneeMechanic() != null) {
            notificationService.notifyUser(ticket.getAssigneeMechanic().getUser().getId(), "MAINTENANCE",
                "Ticket Status Updated", "Ticket #" + ticket.getId() + " status changed to " + status);
        }

        notificationService.notifyMaintenanceOfficers("Ticket Status Updated",
            "Ticket #" + ticket.getId() + " status changed to " + status);

        log.info("Ticket {} status updated to {} by {}", ticketId, status, currentUsername);
    }

    public List<MaintenanceTicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<MaintenanceTicketResponse> getOpenTickets() {
        List<TicketStatus> openStatuses = List.of(
            TicketStatus.OPEN,
            TicketStatus.IN_PROGRESS,
            TicketStatus.ON_HOLD
        );
        return ticketRepository.findByStatusInOrderBySeverityAndDate(openStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<MaintenanceTicketResponse> getTicketsByVehicle(Long vehicleId) {
        return ticketRepository.findByVehicleId(vehicleId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<MaintenanceTicketResponse> getTicketsByMechanic(Long mechanicId) {
        return ticketRepository.findByAssigneeMechanicId(mechanicId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<MaintenanceTicketResponse> getMyTickets() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("Current user not found"));

        return ticketRepository.findByFiledByUserId(currentUser.getId()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public MaintenanceTicketResponse getTicketById(Long ticketId) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapToResponse(ticket);
    }

    private MaintenanceTicketResponse mapToResponse(MaintenanceTicket ticket) {
        return MaintenanceTicketResponse.builder()
            .id(ticket.getId())
            .vehicleId(ticket.getVehicle().getId())
            .vehiclePlateNo(ticket.getVehicle().getPlateNo())
            .vehicleModel(ticket.getVehicle().getModel())
            .filedByUserId(ticket.getFiledByUser().getId())
            .filedByUserName(getDisplayName(ticket.getFiledByUser()))
            .filedByRole(ticket.getFiledByUser().getRole().name())
            .assigneeMechanicId(ticket.getAssigneeMechanic() != null ? ticket.getAssigneeMechanic().getId() : null)
            .assigneeMechanicName(ticket.getAssigneeMechanic() != null ? ticket.getAssigneeMechanic().getFullName() : null)
            .assigneeMechanicPhone(ticket.getAssigneeMechanic() != null ? ticket.getAssigneeMechanic().getPhone() : null)
            .status(ticket.getStatus())
            .severity(ticket.getSeverity())
            .title(ticket.getTitle())
            .description(ticket.getDescription())
            .resolutionNotes(ticket.getResolutionNotes())
            .openedAt(ticket.getOpenedAt())
            .closedAt(ticket.getClosedAt())
            .createdAt(ticket.getCreatedAt())
            .updatedAt(ticket.getUpdatedAt())
            .createdBy(ticket.getCreatedBy())
            .updatedBy(ticket.getUpdatedBy())
            .build();
    }

    private String getDisplayName(User user) {
        // Try to get display name from associated profile
        switch (user.getRole()) {
            case DRIVER:
                return user.getUsername(); // Could fetch from Driver entity
            case GUIDE:
                return user.getUsername(); // Could fetch from Guide entity
            default:
                return user.getUsername();
        }
    }
}

package com.safari.safarims.dto.ticket;

import com.safari.safarims.common.enums.TicketStatus;
import com.safari.safarims.common.enums.TicketSeverity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceTicketResponse {

    private Long id;
    private Long vehicleId;
    private String vehiclePlateNo;
    private String vehicleModel;

    private Long filedByUserId;
    private String filedByUserName;
    private String filedByRole;

    private Long assigneeMechanicId;
    private String assigneeMechanicName;
    private String assigneeMechanicPhone;

    private TicketStatus status;
    private TicketSeverity severity;
    private String title;
    private String description;
    private String resolutionNotes;

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

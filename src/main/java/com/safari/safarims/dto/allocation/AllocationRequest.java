package com.safari.safarims.dto.allocation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AllocationRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private Long driverId;
    private Long guideId;
    private Long jeepId;
    private String notes;
}

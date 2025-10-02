package com.safari.safarims.dto.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    @NotNull(message = "Tour package ID is required")
    private Long packageId;

    @NotNull(message = "Requested date is required")
    @FutureOrPresent(message = "Requested date must be today or in the future")
    private LocalDate requestedDate;

    @NotNull(message = "Requested time is required")
    private LocalTime requestedTime;

    private String notes;
}

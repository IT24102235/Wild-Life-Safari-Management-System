package com.safari.safarims.dto.allocation;

import com.safari.safarims.common.enums.AllocationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AllocationResponse {

    private Long id;
    private Long bookingId;
    private String touristName;
    private String packageName;
    private LocalDate requestedDate;
    private LocalTime requestedTime;

    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverLicense;

    private Long guideId;
    private String guideName;
    private String guidePhone;

    private Long jeepId;
    private String jeepPlateNo;
    private String jeepModel;
    private Integer jeepCapacity;

    private AllocationStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

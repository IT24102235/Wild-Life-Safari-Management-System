package com.safari.safarims.dto.jeep;

import com.safari.safarims.common.enums.JeepStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JeepResponse {

    private Long id;
    private String plateNo;
    private String model;
    private Integer capacity;
    private JeepStatus status;

    private Long defaultDriverId;
    private String defaultDriverName;
    private String defaultDriverPhone;
    private String defaultDriverLicense;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Helper fields
    private boolean isAvailable;
    private int activeAllocationsCount;
}

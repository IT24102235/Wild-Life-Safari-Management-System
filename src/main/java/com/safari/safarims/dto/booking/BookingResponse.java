package com.safari.safarims.dto.booking;

import com.safari.safarims.common.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long touristId;
    private String touristName;
    private Long packageId;
    private String packageName;
    private Integer packageDays;
    private Integer packageMaxPeople;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private BookingStatus status;
    private Integer editWindowSeconds;
    private Integer paymentWindowSeconds;
    private Integer version;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Helper fields for frontend
    private boolean canEdit;
    private LocalDateTime editWindowExpires;
    private LocalDateTime paymentWindowExpires;
}

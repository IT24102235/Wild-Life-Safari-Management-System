package com.safari.safarims.dto.payment;

import com.safari.safarims.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private String touristName;
    private String packageName;
    private BigDecimal amount;
    private PaymentStatus status;
    private String method;
    private String txRef;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper fields
    private boolean isExpired;
    private String maskedCardNumber;
}

package com.safari.safarims.dto.tourpackage;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TourPackageResponse {

    private Long id;
    private String name;
    private Integer days;
    private Integer maxPeople;
    private BigDecimal price;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

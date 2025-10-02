package com.safari.safarims.dto.tourpackage;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TourPackageRequest {

    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Number of days is required")
    @Min(value = 1, message = "Number of days must be at least 1")
    @Max(value = 30, message = "Number of days cannot exceed 30")
    private Integer days;

    @NotNull(message = "Maximum people is required")
    @Min(value = 1, message = "Maximum people must be at least 1")
    @Max(value = 50, message = "Maximum people cannot exceed 50")
    private Integer maxPeople;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "9999.99", message = "Price cannot exceed 9999.99")
    private BigDecimal price;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Boolean isActive = true;
}

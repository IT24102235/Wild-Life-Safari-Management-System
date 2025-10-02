package com.safari.safarims.dto.jeep;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JeepRequest {

    @NotBlank(message = "Plate number is required")
    @Size(max = 20, message = "Plate number cannot exceed 20 characters")
    private String plateNo;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model cannot exceed 50 characters")
    private String model;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Long defaultDriverId;
}

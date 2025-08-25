package com.greentrack.carbon_tracker_api.dto.emissionFactorDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmissionFactorCreateRequest {

    @NotNull(message = "Region is required")
    @Size(max = 5, message = "Region code cannot exceed 5 characters")
    private String region;

    @NotNull(message = "Category is required")
    private ActivityCategory category;

    @NotNull(message = "Sub-type is required")
    private ActivitySubType subType;

    @NotNull(message = "Unit is required")
    @Size(max = 15, message = "Unit cannot exceed 15 characters")
    private String unit;

    @NotNull(message = "CO2e factor is required")
    @Positive(message = "CO2e factor must be positive")
    private BigDecimal co2eFactor;

    @Size(max = 500, message = "Methodology cannot exceed 500 characters")
    private String methodology;

    @Size(max = 200, message = "Source cannot exceed 200 characters")
    private String source;
}

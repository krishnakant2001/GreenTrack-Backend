package com.greentrack.carbon_tracker_api.dto.activityDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityUpdateRequest {

    private ActivityCategory category;
    private ActivitySubType subType;

    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @Size(max = 15, message = "Units cannot exceed 15 characters")
    private String unit;

    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    private LocalDateTime activityDate;

    @Size(max = 50, message = "Location cannot exceed 50 characters")
    private String location;

}

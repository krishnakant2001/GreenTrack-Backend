package com.greentrack.carbon_tracker_api.dto.activityDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityCreateRequest {

    @NotNull(message = "Activity category is required")
    private ActivityCategory category;

    @NotNull(message = "Activity sub-type is required")
    private ActivitySubType subType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    @Size(max = 15, message = "Units cannot exceed 15 characters")
    private String unit;

    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    private LocalDateTime activityDate;

    @Size(max = 50, message = "Location cannot exceed 50 characters")
    private String location;

    @Size(max = 50, message = "Client Idempotency Key cannot exceed 50 characters")
    private String clientIdempotencyKey;

}

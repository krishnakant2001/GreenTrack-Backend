package com.greentrack.carbon_tracker_api.dto.activityDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityResponse {
    private String id;
    private String userId;
    private ActivityCategory category;
    private ActivitySubType subType;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal co2Emissions;
    private String emissionFactorRef;
    private String description;
    private LocalDateTime activityDate;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

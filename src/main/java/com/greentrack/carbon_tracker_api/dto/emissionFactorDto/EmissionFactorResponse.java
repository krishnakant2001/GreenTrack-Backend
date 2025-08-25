package com.greentrack.carbon_tracker_api.dto.emissionFactorDto;

import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityResponse;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmissionFactorResponse {
    private String id;
    private String region;
    private ActivityCategory category;
    private ActivitySubType subType;
    private String unit;
    private BigDecimal co2eFactor;
    private String methodology;
    private String source;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

package com.greentrack.carbon_tracker_api.dto.userGoalDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.GoalPeriod;
import com.greentrack.carbon_tracker_api.entities.enums.GoalType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserGoalResponse {

    private String id;
    private String userId;
    private GoalType goalType;
    private ActivityCategory targetCategory;
    private BigDecimal currentValue;
    private BigDecimal targetValue;
    private GoalPeriod period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal progressPercentage;
    private long daysRemaining;
    private boolean isOnTrack;
}

package com.greentrack.carbon_tracker_api.dto.userGoalDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.GoalPeriod;
import com.greentrack.carbon_tracker_api.entities.enums.GoalType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserGoalUpdateRequest {

    private GoalType goalType;
    private ActivityCategory targetCategory;

    @Positive(message = "Target value must be positive")
    private BigDecimal targetValue;

    private GoalPeriod goalPeriod;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

}

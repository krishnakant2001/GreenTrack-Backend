package com.greentrack.carbon_tracker_api.entities;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.GoalPeriod;
import com.greentrack.carbon_tracker_api.entities.enums.GoalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_goals")
public class UserGoal {

    @Id
    private String id;

    @Indexed
    private String userId;

    private GoalType goalType;
    private ActivityCategory targetCategory;
    private BigDecimal targetValue;
    private GoalPeriod period;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal currentValue = BigDecimal.ZERO;

    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted = false;
}

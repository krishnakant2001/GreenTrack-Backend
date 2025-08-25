package com.greentrack.carbon_tracker_api.dto.recommendationDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.RecommendationType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendationResponse {
    private String id;
    private String userId;
    private RecommendationType recommendationType;
    private ActivityCategory relatedCategory;
    private String title;
    private String description;
    private String actionText;
    private BigDecimal potentialCo2eSavings;
    private String impactDescription;
}

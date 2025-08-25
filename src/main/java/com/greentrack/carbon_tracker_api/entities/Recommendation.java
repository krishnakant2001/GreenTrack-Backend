package com.greentrack.carbon_tracker_api.entities;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommendations")
public class Recommendation {

    @Id
    private String id;

    @Indexed
    private String userId;

    // Recommendation content
    private RecommendationType type;
    private ActivityCategory relatedCategory;
    private String title;
    private String description;
    private String actionText; // "Try using public transport once a week"

    // Impact estimation
    private BigDecimal potentialCo2eSavings; // kg CO2e that could be saved
    private String impactDescription; // "Could save 5kg CO2e per week"

}

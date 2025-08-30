package com.greentrack.carbon_tracker_api.entities;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activities")
@Builder
public class Activity {

    @Id
    private String id;

    @Indexed
    private String userId;

    // Core activity data
    private ActivityCategory category;
    private ActivitySubType subType;
    private BigDecimal quantity; // e.g., 20.5 (km, kWh, etc.)
    private String unit; // km, kWh, pieces, etc.

    // Emission calculation results
    private BigDecimal co2eEmissions; // Calculated CO2e in kg
    private String emissionFactorRef; // Reference to the emission factor used
    private String emissionFactorVersion; // Version of emission factor for auditability

    private String description; // Optional user description
    private LocalDateTime activityDate; // When the activity occurred
    private String location; // Optional location info

    private String clientIdempotencyKey; // For preventing duplicates
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted = false;
    private LocalDateTime deletedAt;

}

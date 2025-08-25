package com.greentrack.carbon_tracker_api.entities;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emission_factors")
@CompoundIndex(
        name = "factor_lookup",
        def = "{'region': 1, 'category': 1, 'subtype': 1, 'unit': 1}"
)
public class EmissionFactor {

    @Id
    private String id;

    // Factor identification
    private String region;
    private ActivityCategory category;
    private ActivitySubType subType;
    private String unit;

    // Emission data
    private BigDecimal co2eFactor;
    private String methodology;
    private String source;

    private LocalDateTime createdAt;
    private String createdBy; // Admin who created this factor
    private LocalDateTime updatedAt;
    private String updatedBy;

    private boolean isDeleted = false;
}

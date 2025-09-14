package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.entities.EmissionFactor;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;

import java.math.BigDecimal;
import java.util.Optional;

public interface EmissionCalculationService {

    BigDecimal calculateCo2Emission(String region, ActivityCategory category,
                                    ActivitySubType subType, String unit, BigDecimal quantity);

    Optional<EmissionFactor> findEmissionFactor(String region, ActivityCategory category,
                                                ActivitySubType subType, String unit);

    String getEmissionFactorRef(EmissionFactor factor);

}

package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.entities.EmissionFactor;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import com.greentrack.carbon_tracker_api.repositories.EmissionFactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmissionCalculationService {

    private final EmissionFactorRepository emissionFactorRepository;

    public BigDecimal calculateCo2Emission(String region, ActivityCategory category,
                                           ActivitySubType subType, String unit, BigDecimal quantity) {

        //Find emission factor with fallback method
        EmissionFactor factor = findEmissionFactor(region, category, subType, unit)
                .orElseThrow(() -> new RuntimeException(
                        String.format("No emission factor found for region=%s, category=%s, subType=%s, unit=%s",
                                region, category, subType, unit)));

        //Calculating the CO2e emission
        BigDecimal co2eEmission = quantity.multiply(factor.getCo2eFactor());

        return co2eEmission.setScale(3, RoundingMode.HALF_UP);

    }

    public Optional<EmissionFactor> findEmissionFactor(String region, ActivityCategory category,
                                                       ActivitySubType subType, String unit) {

        //Try region-specific factor first
        Optional<EmissionFactor> factor = emissionFactorRepository.
                findByRegionAndCategoryAndSubTypeAndUnit(region, category, subType, unit);

        if(factor.isPresent()) {
            return factor;
        }

        //Fallback to global factor
        return emissionFactorRepository.
                findByRegionAndCategoryAndSubTypeAndUnit("GLOBAL", category, subType, unit);

    }

    public String getEmissionFactorRef(EmissionFactor factor) {
        return String.format("%s_%s_%s_%s",
                factor.getRegion(), factor.getCategory(), factor.getSubType(), factor.getUnit());
    }
}

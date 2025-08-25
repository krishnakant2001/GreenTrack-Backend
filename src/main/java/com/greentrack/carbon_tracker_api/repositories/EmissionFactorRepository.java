package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.EmissionFactor;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmissionFactorRepository extends MongoRepository<EmissionFactor, String> {

    // Find emission factor for calculation (most important query)
    Optional<EmissionFactor> findByRegionAndCategoryAndSubTypeAndUnit(
            String region, ActivityCategory category, ActivitySubType subType, String unit);

    // Find all factors for a region
    List<EmissionFactor> findByRegionOrderByCategory(String region);

    // Find factors by category
    List<EmissionFactor> findByCategoryOrderBySubType(ActivityCategory category);

    // Find factors by subtype
    List<EmissionFactor> findBySubType(ActivitySubType subType);

    // Find factors by region and category
    List<EmissionFactor> findByRegionAndCategoryOrderBySubType(String region, ActivityCategory category);

    // Check if factor exists
    boolean existsByRegionAndCategoryAndSubTypeAndUnit(
            String region, ActivityCategory category, ActivitySubType subType, String unit);

    // Find all available regions
    @Query(value = "{}", fields = "{ 'region': 1, '_id': 0 }")
    List<String> findDistinctRegions();

    // Find all available units for a category
    @Query(value = "{ 'category': ?0 }", fields = "{ 'unit': 1, '_id': 0 }")
    List<String> findDistinctUnitsByCategory(ActivityCategory category);

    // Get factors created by specific admin
    List<EmissionFactor> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    // Custom query to find best matching factor with fallback logic
    @Query("{ $or: [ " +
            "{ 'region': ?0, 'category': ?1, 'subType': ?2, 'unit': ?3 }, " +
            "{ 'region': 'GLOBAL', 'category': ?1, 'subType': ?2, 'unit': ?3 } " +
            "] }")
    List<EmissionFactor> findBestMatchingFactor(String region, ActivityCategory category,
                                                ActivitySubType subType, String unit);

    // Find global factors as fallback
    List<EmissionFactor> findByRegionAndCategoryAndSubType(
            String region, ActivityCategory category, ActivitySubType subType);


}

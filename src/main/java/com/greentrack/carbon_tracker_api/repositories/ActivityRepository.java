package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.Activity;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.ActivitySubType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    // Find user's activities
    List<Activity> findByUserIdOrderByActivityDateDesc(String userId);

    // Find user's activities with pagination
    Page<Activity> findByUserId(String userId, Pageable pageable);

    // Find by user and category
    List<Activity> findByUserIdAndCategoryOrderByActivityDateDesc(String userId, ActivityCategory category);

    // Find by user and subType
    List<Activity> findByUserIdAndSubTypeOrderByActivityDateDesc(String userId, ActivitySubType subType);

    // Find activities within date range
    List<Activity> findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by category and date range
    List<Activity> findByUserIdAndCategoryAndActivityDateBetweenOrderByActivityDateDesc(
            String userId, ActivityCategory category, LocalDateTime startDate, LocalDateTime endDate);

    // Check for duplicate by idempotence Key
    Optional<Activity> findByUserIdAndClientIdempotencyKey(String userId, String clientIdempotencyKey);

    // Aggregation queries for dashboard
    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0, 'activityDate': { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { '_id': '$category', 'totalEmissions': { $sum: '$co2eEmissions' }, 'count': { $sum: 1 } } }"
    })
    List<CategoryEmissionSummary> getCategoryEmissionsByDateRange(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Get total emissions for user in date range
    @Query("{ 'userId': ?0, 'activityDate': { $gte: ?1, $lte: ?2 } }")
    List<Activity> findUserActivitiesInDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Count activities by user
    long countByUserId(String userId);

    // Find recent activities (last N days)
    @Query("{ 'userId': ?0, 'activityDate': { $gte: ?1 } }")
    List<Activity> findRecentActivities(String userId, LocalDateTime sinceDate);

    // Find high-emission activities (above threshold)
    @Query("{ 'userId': ?0, 'co2eEmissions': { $gte: ?1 } }")
    List<Activity> findHighEmissionActivities(String userId, BigDecimal threshold);

    // Get user's total emissions
    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0 } }",
            "{ $group: { '_id': null, 'totalEmissions': { $sum: '$co2eEmissions' } } }"
    })
    Optional<TotalEmissionsSummary> getUserTotalEmissions(String userId);

    // Interface for aggregation results
    interface CategoryEmissionSummary {
        ActivityCategory get_id();
        BigDecimal getTotalEmissions();
        int getCount();
    }

    interface TotalEmissionsSummary {
        BigDecimal getTotalEmissions();
    }

}

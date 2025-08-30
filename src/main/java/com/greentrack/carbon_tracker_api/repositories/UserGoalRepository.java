package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.UserGoal;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.GoalPeriod;
import com.greentrack.carbon_tracker_api.entities.enums.GoalType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserGoalRepository extends MongoRepository<UserGoal, String> {

    // Find user's goals
    List<UserGoal> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find active goals (not expired)
    List<UserGoal> findByUserIdAndEndDateGreaterThanEqual(String userId, LocalDate currentDate);

    // Find goals by type
    List<UserGoal> findByUserIdAndGoalType(String userId, GoalType goalType);

    // Find goals by category
    List<UserGoal> findByUserIdAndTargetCategory(String userId, ActivityCategory targetCategory);

    // Find goals by period
    List<UserGoal> findByUserIdAndPeriod(String userId, GoalPeriod period);

    // Find current active goals (started and not expired)
    @Query("{ 'userId': ?0, 'startDate': { $lte: ?1 }, 'endDate': { $gte: ?1 } }")
    List<UserGoal> findCurrentActiveGoals(String userId, LocalDate currentDate);

    // Find goals expiring soon
    @Query("{ 'userId': ?0, 'endDate': { $gte: ?1, $lte: ?2 } }")
    List<UserGoal> findGoalsExpiringSoon(String userId, LocalDate today, LocalDate futureDate);

    // Find overlapping goals (same type and category)
    @Query("{ 'userId': ?0, 'goalType': ?1, 'targetCategory': ?2, " +
            "'startDate': { $lte: ?4 }, 'endDate': { $gte: ?3 } }")
    List<UserGoal> findOverlappingGoals(String userId, GoalType goalType,
                                        ActivityCategory targetCategory, LocalDate startDate, LocalDate endDate);

    // Check if user has active goal for specific category
    boolean existsByUserIdAndTargetCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String userId, ActivityCategory targetCategory, LocalDate endDate, LocalDate startDate);

    // Count user's goals
    long countByUserId(String userId);

    // Find goals that need progress update (active goals)
    @Query("{ 'userId': ?0, 'startDate': { $lte: ?1 }, 'endDate': { $gte: ?1 } }")
    List<UserGoal> findGoalsNeedingProgressUpdate(String userId, LocalDate currentDate);
}

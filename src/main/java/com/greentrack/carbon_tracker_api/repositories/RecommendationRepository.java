package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.Recommendation;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.RecommendationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {

    // Find user's recommendations
    List<Recommendation> findByUserIdOrderByIdDesc(String userId);

    // Find by recommendation type
    List<Recommendation> findByUserIdAndType(String userId, RecommendationType type);

    // Find by related category
    List<Recommendation> findByUserIdAndRelatedCategory(String userId, ActivityCategory relatedCategory);

    // Find recommendations for specific category and type
    List<Recommendation> findByUserIdAndTypeAndRelatedCategory(
            String userId, RecommendationType type, ActivityCategory relatedCategory);

    // Get latest N recommendations for user
    @Query(value = "{ 'userId': ?0 }", sort = "{ '_id': -1 }")
    List<Recommendation> findTopByUserId(String userId, Pageable pageable);

    // Count user's recommendations
    long countByUserId(String userId);

    // Check if recommendation already exists
    boolean existsByUserIdAndTypeAndRelatedCategory(
            String userId, RecommendationType type, ActivityCategory relatedCategory);

    // Delete user's recommendations (for cleanup)
    void deleteByUserId(String userId);
}

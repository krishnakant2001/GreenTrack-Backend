package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.entities.Activity;
import com.greentrack.carbon_tracker_api.entities.Recommendation;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.entities.enums.RecommendationType;
import com.greentrack.carbon_tracker_api.repositories.ActivityRepository;
import com.greentrack.carbon_tracker_api.repositories.RecommendationRepository;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ModelMapper modelMapper;

    public List<RecommendationService> getUserRecommendations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByIdDesc(user.getId());
        return recommendations.stream()
                .map(recommendation -> modelMapper.map(recommendation, RecommendationService.class))
                .collect(Collectors.toList());
    }

    public List<RecommendationService> generateRecommendations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clear existing recommendations
        recommendationRepository.deleteByUserId(user.getId());

        // Analyze user's activity pattern (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Activity> recentActivities = activityRepository.findRecentActivities(user.getId(), thirtyDaysAgo);

        // Generate recommendation based on activity analysis
        List<Recommendation> recommendations = analyzeAndGenerateRecommendations(user.getId(), recentActivities);

        // save recommendations
        List<Recommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .map(recommendation -> modelMapper.map(recommendation, RecommendationService.class))
                .collect(Collectors.toList());
    }

    private List<Recommendation> analyzeAndGenerateRecommendations(String userId, List<Activity> activities) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Group activities by category and calculate emissions
        Map<ActivityCategory, BigDecimal> categoryEmissions = activities.stream()
                .collect(Collectors.groupingBy(
                        Activity::getCategory,
                        Collectors.mapping(Activity::getCo2eEmissions,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Generate recommendations for high-emission categories
        categoryEmissions.forEach((category, totalEmissions) -> {
            if (totalEmissions.compareTo(BigDecimal.valueOf(50)) > 0) { // Threshold: 50kg CO2e
                recommendations.addAll(generateCategoryRecommendations(userId, category, totalEmissions));
            }
        });

        //Add general recommendations if no specific one is generated
        if (recommendations.isEmpty()) {
            recommendations.addAll(generateGeneralRecommendations(userId));
        }

        return recommendations;
    }

    private List<Recommendation> generateCategoryRecommendations(String userId, ActivityCategory category, BigDecimal emissions) {
        List<Recommendation> recommendations = new ArrayList<>();

        switch (category) {
            case TRAVEL:
                recommendations.add(Recommendation.builder()
                        .userId(userId)
                        .type(RecommendationType.TRANSPORT_ALTERNATIVE)
                        .relatedCategory(ActivityCategory.TRAVEL)
                        .title("Reduce Travel Emissions")
                        .description("Your travel emissions are high this month")
                        .actionText("Try using public transport or carpooling once a week")
                        .potentialCo2eSavings(emissions.multiply(BigDecimal.valueOf(0.2))) // 20% potential savings
                        .impactDescription(String.format("Could save %.1f kg CO2e per month",
                                emissions.multiply(BigDecimal.valueOf(0.2)).doubleValue()))
                        .build());
                break;

            case ENERGY:
                recommendations.add(Recommendation.builder()
                        .userId(userId)
                        .type(RecommendationType.ENERGY_EFFICIENCY)
                        .relatedCategory(ActivityCategory.ENERGY)
                        .title("Improve Energy Efficiency")
                        .description("Your energy consumption is above average")
                        .actionText("Switch to LED bulbs and unplug devices when not in use")
                        .potentialCo2eSavings(emissions.multiply(BigDecimal.valueOf(0.15))) // 15% potential savings
                        .impactDescription(String.format("Could save %.1f kg CO2e per month",
                                emissions.multiply(BigDecimal.valueOf(0.15)).doubleValue()))
                        .build());
                break;

            case PURCHASES:
                recommendations.add(Recommendation.builder()
                        .userId(userId)
                        .type(RecommendationType.PURCHASE_OPTIMIZATION)
                        .relatedCategory(ActivityCategory.PURCHASES)
                        .title("Make Sustainable Purchases")
                        .description("Consider more sustainable purchasing choices")
                        .actionText("Buy local products and reduce meat consumption")
                        .potentialCo2eSavings(emissions.multiply(BigDecimal.valueOf(0.25))) // 25% potential savings
                        .impactDescription(String.format("Could save %.1f kg CO2e per month",
                                emissions.multiply(BigDecimal.valueOf(0.25)).doubleValue()))
                        .build());
                break;
        }

        return recommendations;
    }

    private List<Recommendation> generateGeneralRecommendations(String userId) {
        List<Recommendation> recommendations = new ArrayList<>();

        Recommendation generalRec = Recommendation.builder()
                .userId(userId)
                .type(RecommendationType.BEHAVIORAL_CHANGE)
                .relatedCategory(null) // since it's general
                .title("Start Your Carbon Journey")
                .description("Great job on tracking your carbon footprint!")
                .actionText("Set a monthly reduction goal to stay motivated")
                .potentialCo2eSavings(BigDecimal.valueOf(10))
                .impactDescription("Small changes can make a big difference")
                .build();

        recommendations.add(generalRec);

        return recommendations;
    }


}

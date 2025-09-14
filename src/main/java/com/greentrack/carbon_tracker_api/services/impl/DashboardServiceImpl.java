package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.dto.dashboardDto.DashboardSummaryResponse;
import com.greentrack.carbon_tracker_api.entities.Activity;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.repositories.ActivityRepository;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public DashboardSummaryResponse getDashboardSummary(String userEmail, String period) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate[] dateRange = calculateDateRange(period);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get current period activities
        List<Activity> currentActivities = activityRepository
                .findUserActivitiesInDateRange(user.getId(), startDateTime, endDateTime);

        // Get previous period for comparison
        LocalDate[] previousDateRange = calculatePreviousDateRange(period, startDate);
        LocalDateTime prevStartDateTime = previousDateRange[0].atStartOfDay();
        LocalDateTime prevEndDateTime = previousDateRange[1].atTime(23, 59, 59);

        List<Activity> previousActivities = activityRepository
                .findUserActivitiesInDateRange(user.getId(), prevStartDateTime, prevEndDateTime);

        return buildDashboardSummary(user.getId(), currentActivities, previousActivities, period, endDate);
    }

    private LocalDate[] calculateDateRange(String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (period.toLowerCase()) {
            case "weekly":
                startDate = today.minusWeeks(1);
                break;
            case "monthly":
                startDate = today.minusMonths(1);
                break;
            case "yearly":
                startDate = today.minusYears(1);
                break;
            default:
                startDate = today.minusDays(7);
        }
        return new LocalDate[]{startDate, today};
    }

    private LocalDate[] calculatePreviousDateRange(String period, LocalDate currentStart) {
        LocalDate prevStart;
        LocalDate prevEnd;

        switch (period.toLowerCase()) {
            case "weekly":
                prevStart = currentStart.minusWeeks(1);
                prevEnd = currentStart.minusDays(1);
                break;
            case "monthly":
                prevStart = currentStart.minusMonths(1);
                prevEnd = currentStart.minusDays(1);
                break;
            case "yearly":
                prevStart = currentStart.minusYears(1);
                prevEnd = currentStart.minusDays(1);
                break;
            default:
                prevStart = currentStart.minusWeeks(1);
                prevEnd = currentStart.minusDays(1);
        }

        return new LocalDate[]{prevStart, prevEnd};
    }

    private DashboardSummaryResponse buildDashboardSummary(String userId, List<Activity> currentActivities,
                                                           List<Activity> previousActivities, String period,
                                                           LocalDate summaryDate) {
        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .userId(userId)
                .summaryDate(summaryDate)
                .period(period)
                .build();

        // calculate total emission
        BigDecimal totalEmissions = currentActivities.stream()
                .map(activity -> activity.getCo2eEmissions())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalCo2eEmissions(totalEmissions);

        // calculate category breakdown
        Map<ActivityCategory, BigDecimal> categoryBreakdown = currentActivities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getCategory(),
                        Collectors.mapping(activity -> activity.getCo2eEmissions(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        response.setCategoryBreakdown(categoryBreakdown);

        // calculate activity counts
        response.setTotalActivities(currentActivities.size());
        Map<ActivityCategory, Integer> activityCounts = currentActivities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getCategory(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
        response.setActivityCounts(activityCounts);

        // previous period comparison
        BigDecimal previousEmissions = previousActivities.stream()
                .map(activity -> activity.getCo2eEmissions())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setPreviousPeriodEmissions(previousEmissions);

        if(previousEmissions.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = totalEmissions.subtract(previousEmissions)
                    .divide(previousEmissions, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setPercentageChange(change);
            response.setImprovement(change.compareTo(BigDecimal.ZERO) < 0);
        } else {
            response.setPercentageChange(BigDecimal.ZERO);
            response.setImprovement(false);
        }

        // Insights
        ActivityCategory topCategory = categoryBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        response.setTopCategory(topCategory != null ? topCategory.toString() : "None");

        // Improvement suggestion
        if (topCategory != null) {
            switch (topCategory) {
                case TRAVEL:
                    response.setImprovementArea("Consider using public transport or walking");
                    break;
                case ENERGY:
                    response.setImprovementArea("Try reducing energy consumption at home");
                    break;
                case PURCHASES:
                    response.setImprovementArea("Focus on sustainable purchasing choices");
                    break;
                default:
                    response.setImprovementArea("Keep tracking to identify improvement areas");
            }
        } else {
            response.setImprovementArea("Start logging activities to get insights");
        }

        return response;
    }
}

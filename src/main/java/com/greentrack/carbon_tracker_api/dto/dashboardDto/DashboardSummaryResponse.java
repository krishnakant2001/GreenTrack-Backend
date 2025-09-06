package com.greentrack.carbon_tracker_api.dto.dashboardDto;

import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {

    private String userId;
    private LocalDate summaryDate;
    private String period; // "daily", "weekly", "monthly"

    // Emission totals
    private BigDecimal totalCo2eEmissions;
    private Map<ActivityCategory, BigDecimal> categoryBreakdown;

    // Comparisons
    private BigDecimal previousPeriodEmissions;
    private BigDecimal percentageChange;
    private boolean isImprovement;

    // Activity counts
    private int totalActivities;
    private Map<ActivityCategory, Integer> activityCounts;

    // Insights
    private String topCategory; // Highest emission category
    private String improvementArea; // Category with potential for reduction


}

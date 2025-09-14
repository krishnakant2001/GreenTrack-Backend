package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.dashboardDto.DashboardSummaryResponse;
import com.greentrack.carbon_tracker_api.entities.Activity;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary(String userEmail, String period);
}

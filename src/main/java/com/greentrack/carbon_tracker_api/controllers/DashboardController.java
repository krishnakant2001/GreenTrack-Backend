package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.dashboardDto.DashboardSummaryResponse;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.services.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(defaultValue = "monthly") String period) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DashboardSummaryResponse summaryResponse = dashboardService.getDashboardSummary(user.getEmail(), period);
        return ResponseEntity.ok(ApiResponse.success(summaryResponse));
    }

}

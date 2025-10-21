package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.recommendationDto.RecommendationResponse;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.services.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/user/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getUserRecommendations() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RecommendationResponse> recommendations = recommendationService.getUserRecommendations(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(recommendations));

    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> generateRecommendations() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<RecommendationResponse> recommendations = recommendationService.generateRecommendations(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Recommendation generate successfully", recommendations));

    }
}

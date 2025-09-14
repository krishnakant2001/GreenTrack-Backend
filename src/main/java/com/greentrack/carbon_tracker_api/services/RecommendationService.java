package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.recommendationDto.RecommendationResponse;

import java.util.List;

public interface RecommendationService {

    List<RecommendationResponse> getUserRecommendations(String userEmail);

    List<RecommendationResponse> generateRecommendations(String userEmail);

}

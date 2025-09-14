package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalCreateRequest;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalResponse;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalUpdateRequest;

import java.util.List;

public interface UserGoalService {

    UserGoalResponse createGoal(String userEmail, UserGoalCreateRequest request);

    List<UserGoalResponse> getUserGoals(String userEmail);

    List<UserGoalResponse> getActiveGoals(String userEmail);

    UserGoalResponse updateGoal(String userEmail, String goalId, UserGoalUpdateRequest request);

    void deleteGoal(String userEmail, String goalId);

}

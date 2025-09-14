package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalCreateRequest;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalResponse;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalUpdateRequest;
import com.greentrack.carbon_tracker_api.entities.Activity;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.entities.UserGoal;
import com.greentrack.carbon_tracker_api.repositories.ActivityRepository;
import com.greentrack.carbon_tracker_api.repositories.UserGoalRepository;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.services.UserGoalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGoalServiceImpl implements UserGoalService {

    private final UserGoalRepository userGoalRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ModelMapper modelMapper;

    public UserGoalResponse createGoal(String userEmail, UserGoalCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not Runtime"));

        //Check for overlapping goals
        List<UserGoal> overlapping = userGoalRepository.findOverlappingGoals(user.getId(),
                request.getGoalType(), request.getTargetCategory(), request.getStartDate(), request.getEndDate());

        if(!overlapping.isEmpty()) {
            throw new RuntimeException("You already have a similar goal for this period");
        }

        //Calculate baseline emissions
        BigDecimal baselineValue = calculateBaselineEmission(user.getId(), request);

        UserGoal goal = UserGoal.builder()
                .userId(user.getId())
                .goalType(request.getGoalType())
                .targetCategory(request.getTargetCategory())
                .period(request.getGoalPeriod())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .title(request.getTitle())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        goal.setCurrentValue(calculateCurrentProgress(user.getId(), goal));

        UserGoal savedGoal = userGoalRepository.save(goal);
        return mapToUserGoalResponse(savedGoal);
    }

    public List<UserGoalResponse> getUserGoals(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserGoal> goals = userGoalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return goals.stream()
                .map(goal -> mapToUserGoalResponse(goal))
                .collect(Collectors.toList());
    }

    public List<UserGoalResponse> getActiveGoals(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserGoal> goals = userGoalRepository.findCurrentActiveGoals(user.getId(), LocalDate.now());
        return goals.stream()
                .map(goal -> mapToUserGoalResponse(goal))
                .collect(Collectors.toList());
    }

    public UserGoalResponse updateGoal(String userEmail, String goalId, UserGoalUpdateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserGoal goal = userGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if(!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Goal not found");
        }

        // Update fields if provided
        if (request.getGoalType() != null) {
            goal.setGoalType(request.getGoalType());
        }
        if (request.getTargetCategory() != null) {
            goal.setTargetCategory(request.getTargetCategory());
        }
        if (request.getTargetValue() != null) {
            goal.setTargetValue(request.getTargetValue());
        }
        if (request.getGoalPeriod() != null) {
            goal.setPeriod(request.getGoalPeriod());
        }
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            goal.setEndDate(request.getEndDate());
        }
        if (request.getTitle() != null) {
            goal.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }

        //Recalculate current progress
        goal.setCurrentValue(calculateCurrentProgress(user.getId(), goal));
        goal.setUpdatedAt(LocalDateTime.now());

        UserGoal updatedGoal = userGoalRepository.save(goal);
        return mapToUserGoalResponse(updatedGoal);

    }

    public void deleteGoal(String userEmail, String goalId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserGoal goal = userGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if(!goal.getUserId().equals(user.getId())) {
            throw new RuntimeException("Goal not found");
        }

        userGoalRepository.delete(goal);
    }

    private BigDecimal calculateBaselineEmission(String userId, UserGoalCreateRequest request) {

        //Calculate baseline based on previous period
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<Activity> activities;

        if(request.getTargetCategory() != null) {
            activities = activityRepository.findByUserIdAndCategoryAndActivityDateBetweenOrderByActivityDateDesc(
                    userId, request.getTargetCategory(), startDateTime, endDateTime);
        } else {
            activities = activityRepository.findUserActivitiesInDateRange(userId, startDateTime, endDateTime);
        }

        return activities.stream()
                .map(activity -> activity.getCo2eEmissions())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCurrentProgress(String userId, UserGoal goal) {

        LocalDateTime startDateTime = goal.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        List<Activity> activities;

        if(goal.getTargetCategory() != null) {
            activities = activityRepository.findByUserIdAndCategoryAndActivityDateBetweenOrderByActivityDateDesc(
                    userId, goal.getTargetCategory(), startDateTime, endDateTime);
        } else {
            activities = activityRepository.findUserActivitiesInDateRange(userId, startDateTime, endDateTime);
        }

        return activities.stream()
                .map(activity -> activity.getCo2eEmissions())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    private UserGoalResponse mapToUserGoalResponse(UserGoal goal) {
        UserGoalResponse goalResponse = modelMapper.map(goal, UserGoalResponse.class);

        //Calculate derived fields
        if(goal.getTargetValue() != null && goal.getTargetValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = goal.getCurrentValue()
                    .divide(goal.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            goalResponse.setProgressPercentage(progress);
        }

        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getEndDate());
        goalResponse.setDaysRemaining(Math.max(0, daysRemaining));

        //Simple on track Calculation
        long totalDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate());
        long daysPassed = ChronoUnit.DAYS.between(goal.getStartDate(), LocalDate.now());

        if(totalDays > 0) {
            BigDecimal expectedProgress = goal.getTargetValue()
                    .multiply(BigDecimal.valueOf(daysPassed))
                    .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);

            goalResponse.setOnTrack(goal.getCurrentValue().compareTo(expectedProgress) >= 0);
        }

        return goalResponse;

    }
}

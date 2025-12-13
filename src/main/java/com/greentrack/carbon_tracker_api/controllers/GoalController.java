package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalCreateRequest;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalResponse;
import com.greentrack.carbon_tracker_api.dto.userGoalDto.UserGoalUpdateRequest;
import com.greentrack.carbon_tracker_api.services.UserGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/user/goals")
@RequiredArgsConstructor
public class GoalController {

    private final UserGoalService userGoalService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserGoalResponse>> createGoal(@Valid @RequestBody UserGoalCreateRequest request) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            log.info("Goal creation process started....");

            UserGoalResponse goalResponse = userGoalService.createGoal(user.getEmail(), request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Goal created Successfully", goalResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserGoalResponse>>> getUserGoals() {
        UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Getting all goals for user: {}", user.getId());

        List<UserGoalResponse> goals = userGoalService.getUserGoals(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserGoalResponse>>> getActiveGoals() {
        UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Getting active goals for user: {}", user.getId());

        List<UserGoalResponse> goals = userGoalService.getActiveGoals(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<UserGoalResponse>> updateGoal(
            @PathVariable String goalId, @Valid @RequestBody UserGoalUpdateRequest request) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            log.info("User: {} updating goal by goalId: {}", user.getId(), goalId);

            UserGoalResponse updatedGoal = userGoalService.updateGoal(user.getEmail(), goalId, request);

            return ResponseEntity.ok(ApiResponse.success("Goal updated Successfully", updatedGoal));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<String>> deleteGoal(@PathVariable String goalId) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            log.info("User: {} deleting goal by goalId: {}", user.getId(), goalId);

            userGoalService.deleteGoal(user.getEmail(), goalId);
            return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

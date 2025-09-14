package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityCreateRequest;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityResponse;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityUpdateRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.services.impl.ActivityServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityServiceImpl activityService;

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            ActivityResponse activity = activityService.createActivity(user.getEmail(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Activity created successfully", activity));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getUserActivities() {
        UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<ActivityResponse> activities = activityService.getUserActivities(user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(activities));
    }


    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getUserActivitiesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityResponse> activities = activityService.getUserActivitiesPaginated(user.getEmail(), pageable);

        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable String activityId) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            ActivityResponse activity = activityService.getActivityById(user.getEmail(), activityId);
            return ResponseEntity.ok(ApiResponse.success(activity));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(@PathVariable String activityId,
            @Valid @RequestBody ActivityUpdateRequest request) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            ActivityResponse activity = activityService.updateActivity(user.getEmail(), activityId, request);
            return ResponseEntity.ok(ApiResponse.success("Activity updated successfully", activity));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<ApiResponse<String>> deleteActivity(@PathVariable String activityId) {
        try {
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            activityService.deleteActivity(user.getEmail(), activityId);
            return ResponseEntity.ok(ApiResponse.success("Activity deleted successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

}

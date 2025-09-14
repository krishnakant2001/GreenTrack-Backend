package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityCreateRequest;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityResponse;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ActivityService {

    ActivityResponse createActivity(String userEmail, ActivityCreateRequest request);

    List<ActivityResponse> getUserActivities(String userEmail);

    Page<ActivityResponse> getUserActivitiesPaginated(String userEmail, Pageable pageable);

    ActivityResponse getActivityById(String userEmail, String activityId);

    ActivityResponse updateActivity(String userEmail, String activityId, ActivityUpdateRequest request);

    void deleteActivity(String userEmail, String activityId);

}

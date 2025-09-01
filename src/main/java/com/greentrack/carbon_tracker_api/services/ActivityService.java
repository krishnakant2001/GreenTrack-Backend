package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityCreateRequest;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityResponse;
import com.greentrack.carbon_tracker_api.dto.activityDto.ActivityUpdateRequest;
import com.greentrack.carbon_tracker_api.entities.Activity;
import com.greentrack.carbon_tracker_api.entities.EmissionFactor;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.repositories.ActivityRepository;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final EmissionCalculationService emissionCalculationService;
    private final ModelMapper modelMapper;

    public ActivityResponse createActivity(String userEmail, ActivityCreateRequest request) {

        //Get User
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Check for duplicate using idempotency key
        if(request.getClientIdempotencyKey() != null) {
            Optional<Activity> existing = activityRepository
                    .findByUserIdAndClientIdempotencyKey(user.getId(), request.getClientIdempotencyKey());

            if(existing.isPresent()) {
                return modelMapper.map(existing.get(), ActivityResponse.class);
            }
        }

        //Calculate CO2e emissions
        BigDecimal co2eEmissions = emissionCalculationService
                .calculateCo2Emission(user.getRegion(), request.getCategory(), request.getSubType(),
                        request.getUnit(), request.getQuantity());

        //Get emission factor for a reference
        EmissionFactor factor = emissionCalculationService
                .findEmissionFactor(user.getRegion(), request.getCategory(), request.getSubType(), request.getUnit())
                .orElseThrow(() -> new RuntimeException("Emission factor not found"));

        //Create Activity
        Activity activity = Activity.builder()
                .userId(user.getId())
                .category(request.getCategory())
                .subType(request.getSubType())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .co2eEmissions(co2eEmissions)
                .emissionFactorRef(emissionCalculationService.getEmissionFactorRef(factor))
                .emissionFactorVersion("v1.0")
                .description(request.getDescription())
                .activityDate(request.getActivityDate() != null ? request.getActivityDate() : LocalDateTime.now())
                .location(request.getLocation())
                .clientIdempotencyKey(request.getClientIdempotencyKey())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Activity savedActivity = activityRepository.save(activity);
        return modelMapper.map(savedActivity, ActivityResponse.class);

    }


    public List<ActivityResponse> getUserActivities(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Activity> activities = activityRepository.findByUserIdOrderByActivityDateDesc(user.getId());

        return activities.stream()
                .map(activity -> modelMapper.map(activity, ActivityResponse.class))
                .collect(Collectors.toList());
    }


    public Page<ActivityResponse> getUserActivitiesPaginated(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Activity> activities = activityRepository.findByUserId(user.getId(), pageable);

        return activities.map(activity -> modelMapper.map(activity, ActivityResponse.class));
    }


    public ActivityResponse getActivityById(String userEmail, String activityId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->  new RuntimeException("Activity not found"));

        //check if activity belongs to user
        if(!activity.getUserId().equals(user.getId())) {
            throw new RuntimeException("Activity not found");
        }

        return modelMapper.map(activity, ActivityResponse.class);
    }


    public ActivityResponse updateActivity(String userEmail, String activityId, ActivityUpdateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->  new RuntimeException("Activity not found"));

        //check if activity belongs to user
        if (!activity.getUserId().equals(user.getId())) {
            throw new RuntimeException("Activity not found");
        }

        // Update fields if provided
        boolean needsRecalculation = request.getCategory() != null || request.getSubType() != null ||
                request.getQuantity() != null || request.getUnit() != null;

        if(request.getCategory() != null) activity.setCategory(request.getCategory());
        if(request.getSubType() != null) activity.setSubType(request.getSubType());
        if(request.getQuantity() != null) activity.setQuantity(request.getQuantity());
        if(request.getUnit() != null) activity.setUnit(request.getUnit());
        if(request.getDescription() != null) activity.setDescription(request.getDescription());
        if(request.getActivityDate() != null) activity.setActivityDate(request.getActivityDate());
        if(request.getLocation() != null) activity.setLocation(request.getLocation());

        // Recalculate emissions if needed
        if(needsRecalculation) {
            BigDecimal co2eEmissions = emissionCalculationService
                    .calculateCo2Emission(user.getRegion(), activity.getCategory(), activity.getSubType(),
                            activity.getUnit(), activity.getQuantity());

            activity.setCo2eEmissions(co2eEmissions);
        }

        activity.setUpdatedAt(LocalDateTime.now());
        Activity updatedActivity = activityRepository.save(activity);

        return modelMapper.map(updatedActivity, ActivityResponse.class);
    }


    public void deleteActivity(String userEmail, String activityId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        //check if activity belongs to user
        if(!activity.getUserId().equals(user.getId())) {
            throw new RuntimeException("Activity not found");
        }

        activityRepository.delete(activity);
    }
}

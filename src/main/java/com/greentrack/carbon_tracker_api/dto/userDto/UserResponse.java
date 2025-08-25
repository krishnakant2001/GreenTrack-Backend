package com.greentrack.carbon_tracker_api.dto.userDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String region;
    private Boolean isActive;
    private LocalDateTime CreatedAt;
    private LocalDateTime lastLoginAt;
}

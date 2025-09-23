package com.greentrack.carbon_tracker_api.dto.userDto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name cannot exceed 20 characters")
    private String lastName;

    @NotBlank
    @Size(max = 5, message = "Region code cannot exceed 5 characters")
    private String region;

    @Nullable
    private String currentPassword;

    @Nullable
    private String newPassword;
}

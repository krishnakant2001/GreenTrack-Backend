package com.greentrack.carbon_tracker_api.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name cannot exceed 20 characters")
    private String lastName;

    @NotBlank(message = "Region is required")
    @Size(max = 5, message = "Region code cannot exceed 5 characters")
    private String region;

}

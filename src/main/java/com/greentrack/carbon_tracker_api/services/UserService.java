package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserRegistrationRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {

    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    AuthResponse registerUser(UserRegistrationRequest request);

    UserResponse getUserById(String id);

    UserResponse getUserProfile(String email);

    UserResponse updateUserProfile(String email, UserUpdateRequest request);

    void deleteUser(String email);

}

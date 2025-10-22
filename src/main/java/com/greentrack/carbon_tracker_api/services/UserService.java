package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.OtpDto.VerifyOtpRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserRegistrationRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserUpdateRequest;
import com.greentrack.carbon_tracker_api.entities.User;

import java.util.Map;

public interface UserService {

    AuthResponse registerUser(UserRegistrationRequest request);

    Map<String, Object> initiateRegistration(UserRegistrationRequest request);

    AuthResponse verifyOtpAndRegisterUser(VerifyOtpRequest request);

    UserResponse getUserById(String id);

    UserResponse getUserProfile(String email);

    UserResponse updateUserProfile(String email, UserUpdateRequest request);

    User getUserByEmail(String email);

    User savedNewUser(User user);

    void deleteUser(String email);

}

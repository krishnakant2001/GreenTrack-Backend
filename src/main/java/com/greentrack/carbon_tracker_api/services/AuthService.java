package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserLoginRequest;

public interface AuthService {
    AuthResponse loginUser(UserLoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}

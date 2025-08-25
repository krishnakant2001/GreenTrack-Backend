package com.greentrack.carbon_tracker_api.dto.userDto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse(String token, UserResponse user){
        this.token = token;
        this.user = user;
    }
}

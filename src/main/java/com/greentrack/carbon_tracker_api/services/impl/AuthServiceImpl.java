package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserLoginRequest;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.security.JwtService;
import com.greentrack.carbon_tracker_api.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse loginUser(UserLoginRequest request) {

        log.info("Login process started....");

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User successfully logged in....");

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userId = jwtService.getUserIdFromRefreshToken(refreshToken);

        //validate the refresh token is present in the db for session handling

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not Found"));

        String token = jwtService.generateToken(user);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(token, refreshToken);
    }
}

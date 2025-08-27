package com.greentrack.carbon_tracker_api.services;

import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserLoginRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    public AuthResponse loginUser(UserLoginRequest request) {

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

//        User userExact = (User) authentication.getPrincipal();

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not Found"));


        String token = jwtService.generateToken(user);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(token, modelMapper.map(user, UserResponse.class));
    }
}

package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserRegistrationRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserUpdateRequest;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.security.JwtService;
import com.greentrack.carbon_tracker_api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("user with email " + email + " not found"));
    }

    public AuthResponse registerUser(UserRegistrationRequest request) {

        //check if email already exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already registered");
        }

        //create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        //Generate JWT token
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, modelMapper.map(savedUser, UserResponse.class));

    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info(user.getUsername());
        log.info(user.getPassword());
        log.info(user.getPasswordHash());
        Boolean flag = (Boolean) passwordEncoder.matches("Test@1234", user.getPassword());
        if(flag) log.info("Password Matched");
        else log.info("Password does not matched");
        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse updateUserProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Update fields if provided
        if(!request.getFirstName().isEmpty()){
            user.setFirstName(request.getFirstName());
        }
        if(!request.getLastName().isEmpty()){
            user.setLastName(request.getLastName());
        }
        if(!request.getRegion().isEmpty()){
            user.setRegion(request.getRegion());
        }
        if (request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty() &&
                request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {

            if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 100) {
                throw new RuntimeException("New password must be between 6 and 100 characters");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Your current password is incorrect");
            }

            if (request.getCurrentPassword().equals(request.getNewPassword())) {
                throw new RuntimeException("New password cannot be the same as current password");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserResponse.class);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}

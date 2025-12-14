package com.greentrack.carbon_tracker_api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.greentrack.carbon_tracker_api.dto.OtpDto.VerifyOtpRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserRegistrationRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserUpdateRequest;
import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.repositories.UserRepository;
import com.greentrack.carbon_tracker_api.security.JwtService;
import com.greentrack.carbon_tracker_api.security.SessionService;
import com.greentrack.carbon_tracker_api.services.OtpService;
import com.greentrack.carbon_tracker_api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpService otpService;
    private final SessionService sessionService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("user with email " + email + " not found"));
    }

    public Map<String, Object> initiateRegistration(UserRegistrationRequest request) {
        //check if email already exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already registered");
        }

        log.info("Registration process started....");

        String normalizedEmail = request.getEmail().toLowerCase().trim();

        // Send OTP
        Map<String, Object> otpResponse = otpService.sendOtp(normalizedEmail);

        // Store pending registration data (for 15 minutes)
        String regKey = "REGISTRATION:" + normalizedEmail;
        redisTemplate.opsForValue().set(regKey, request, 10, TimeUnit.MINUTES);

        otpResponse.put("email", normalizedEmail);
        otpResponse.put("message", "OTP sent successfully. Proceed to verification.");

        return otpResponse;
    }
    public AuthResponse verifyOtpAndRegisterUser(VerifyOtpRequest verifyOtpRequest) {

        String normalizedEmail = verifyOtpRequest.getEmail().toLowerCase().trim();

        log.info("Initiate the verification process of OTP....");

        Map<String, Object> res = otpService.verifyOtp(normalizedEmail, verifyOtpRequest.getOtp());
        boolean isSuccess = Boolean.TRUE.equals(res.get("success"));

        if(!isSuccess) {
            throw new RuntimeException(String.valueOf(res.get("message")));
        }

        String regKey = "REGISTRATION:" + normalizedEmail;
        Object registrationObj = redisTemplate.opsForValue().get(regKey);

        UserRegistrationRequest request = new ObjectMapper().registerModule(new JavaTimeModule())
                .convertValue(registrationObj, UserRegistrationRequest.class);

        if (request == null) {
            throw new RuntimeException("Registration session expired. Please restart registration.");
        }

        //create new user
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRegion(request.getRegion());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        user.setRoles(new HashSet<>(Collections.singleton("ROLE_USER")));

        User savedUser = userRepository.save(user);

        //Delete registration cache
        redisTemplate.delete(regKey);

        //Create sessionId
        String sessionId = UUID.randomUUID().toString();

        //Generate JWT token
        String token = jwtService.generateToken(savedUser, sessionId);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        sessionService.generateNewSession(user.getId(), refreshToken, sessionId);

        log.info("Registration successfully done....");

        return new AuthResponse(token, refreshToken);
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

        //Create sessionId
        String sessionId = UUID.randomUUID().toString();

        //Generate JWT token
        String token = jwtService.generateToken(savedUser, sessionId);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        sessionService.generateNewSession(user.getId(), refreshToken, sessionId);

        return new AuthResponse(token, refreshToken);

    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return modelMapper.map(user, UserResponse.class);
    }

    @Cacheable(value = "user-profiles", key = "#email")
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return modelMapper.map(user, UserResponse.class);
    }

    @CacheEvict(value = "user-profiles", key = "#email")
    public UserResponse updateUserProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        log.info("User profile updating started....");
        log.info("Updating user email: {}", email);

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

        log.info("User profile updated successfully....");

        return modelMapper.map(updatedUser, UserResponse.class);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User savedNewUser(User newUser) {
        return userRepository.save(newUser);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        userRepository.delete(user);
    }
}

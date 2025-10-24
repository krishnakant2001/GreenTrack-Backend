package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserUpdateRequest;
import com.greentrack.carbon_tracker_api.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/getProfileDetails")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile() {
        try {
            log.info("Getting user profile details");
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponse userResponse = userService.getUserProfile(user.getEmail());
            return ResponseEntity.ok(ApiResponse.success(userResponse));
        } catch (Exception e) {
            log.error("Error while fetching user profile", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch user profile"));
        }
    }

    @PutMapping("/updateProfileDetails")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(@Valid @RequestBody UserUpdateRequest request) {
        try {
            log.info("Updating user profile details");
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponse userResponse = userService.updateUserProfile(user.getEmail(), request);
            return ResponseEntity.ok(ApiResponse.success("Profile Updated Successfully", userResponse));
        } catch (RuntimeException e) {
            log.error("Error while updating user profile", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error while updating user profile when password not given", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update the profile"));
        }
    }

    @DeleteMapping("/deleteProfile")
    public ResponseEntity<ApiResponse<String>> deleteAccount() {
        try {
            log.info("Deleting user profile account");
            UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userService.deleteUser(user.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
        } catch (Exception e) {
            log.error("Error while deleting account", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete account"));
        }
    }
}

package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.OtpDto.OtpResponse;
import com.greentrack.carbon_tracker_api.dto.OtpDto.SendOtpRequest;
import com.greentrack.carbon_tracker_api.dto.OtpDto.VerifyOtpRequest;
import com.greentrack.carbon_tracker_api.services.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpService otpService;

    @PostMapping(path = "/otpSent")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(
            @Valid @RequestBody SendOtpRequest sendOtpRequest, BindingResult bindingResult) {

        try {
            // Check if there were validation errors
            if (bindingResult.hasErrors()) {
                // Get all error messages
                String errors = bindingResult.getAllErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(ApiResponse.error(errors));
            }

            log.info("Received request to send OTP to email: {}", sendOtpRequest.getEmail());

            Map<String, Object> result = otpService.sendOtp(sendOtpRequest.getEmail());

            boolean isSuccess = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.success("OTP send", result));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(message));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send the OTP"));
        }
    }

    @PostMapping(path = "/verifyOtp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest, BindingResult bindingResult) {

        try {
            if (bindingResult.hasErrors()) {
                // Get all error messages
                String errors = bindingResult.getAllErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(ApiResponse.error(errors));
            }

            log.info("Received request to verify OTP for email: {}", verifyOtpRequest.getEmail());

            Map<String, Object> result = otpService.verifyOtp(
                            verifyOtpRequest.getEmail(),
                            verifyOtpRequest.getOtp()
            );

            boolean isSuccess = (boolean) result.get("success");
            String message = (String) result.get("message");

            if(isSuccess) {
                return ResponseEntity.ok(ApiResponse.success("OTP verified", result));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(message));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(path = "/resendOtp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendOtp(
            @Valid @RequestBody SendOtpRequest sendOtpRequest, BindingResult bindingResult) {

        try {
            // Check if there were validation errors
            if (bindingResult.hasErrors()) {
                // Get all error messages
                String errors = bindingResult.getAllErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(ApiResponse.error(errors));
            }

            log.info("Received request to resend OTP to email: {}", sendOtpRequest.getEmail());

            Map<String, Object> result = otpService.resendOtp(sendOtpRequest.getEmail());

            boolean isSuccess = (boolean) result.get("success");
            String message = (String) result.get("message");

            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.success("OTP resend", result));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(message));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to resend the OTP"));
        }
    }

    @GetMapping(path = "/check-verification/{email}")
    public ResponseEntity<ApiResponse<OtpResponse>> checkVerification(@PathVariable String email) {

        log.info("Checking verification status for email: {}", email);

        try {
            boolean isVerified = otpService.isEmailVerified(email);

            OtpResponse res = new OtpResponse(isVerified, "Email is verified");

            if(!isVerified) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Email is not verified"));
            }
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Email is verified", res));

        } catch (Exception e) {
            log.error("Error checking verification status for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check verification status. Please try again."));
        }
    }

}

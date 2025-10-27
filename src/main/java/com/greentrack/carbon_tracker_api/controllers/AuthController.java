package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.OtpDto.VerifyOtpRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.AuthResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserLoginRequest;
import com.greentrack.carbon_tracker_api.dto.userDto.UserRegistrationRequest;
import com.greentrack.carbon_tracker_api.services.AuthService;
import com.greentrack.carbon_tracker_api.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/initiate-registration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initialRegister(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            Map<String, Object> otpResponse = userService.initiateRegistration(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("OTP sent", otpResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verifyOtpAndRegister")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtpAndRegisterUser(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            AuthResponse authResponse = userService.verifyOtpAndRegisterUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered Successfully", authResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            AuthResponse authResponse = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered Successfully", authResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.loginUser(request);

            Cookie cookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            cookie.setHttpOnly(true);

            response.addCookie(cookie);

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {

        try {
            //fetch refresh token from the cookies
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

            AuthResponse authResponse = authService.refreshToken(refreshToken);

            return ResponseEntity.ok(ApiResponse.success("JWT token updated", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token is expired, Please login again"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            //fetch refresh token from the cookies
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

            authService.logoutUser(refreshToken);

            return ResponseEntity.ok(ApiResponse.success("User successfully logout"));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal Server Error"));
        }
    }


}

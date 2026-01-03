package com.greentrack.carbon_tracker_api.services;

import java.util.Map;

public interface OtpService {
    void sendOtp(String email);
    Map<String, Object> verifyOtp(String email, String otp);
    void resendOtp(String email);
    Boolean isEmailVerified(String email);
    void clearVerification(String email);
}

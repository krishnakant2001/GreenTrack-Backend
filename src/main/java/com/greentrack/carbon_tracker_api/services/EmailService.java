package com.greentrack.carbon_tracker_api.services;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}

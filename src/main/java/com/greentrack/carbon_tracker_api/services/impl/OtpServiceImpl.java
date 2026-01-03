package com.greentrack.carbon_tracker_api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.greentrack.carbon_tracker_api.model.OtpData;
import com.greentrack.carbon_tracker_api.services.EmailService;
import com.greentrack.carbon_tracker_api.services.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_PREFIX = "otp:";
    private static final String VERIFIED_PREFIX = "verified:";

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    @Value("${otp.max.attempts}")
    private int maxAttempts;

    @Value("${otp.length}")
    private int otpLength;

    //Generate and Send OTP to email
    @Async
    public void sendOtp(String email) {
        try{
            String normalizedEmail = email.toLowerCase().trim();

            // Delete any existing OTP for this email
            String otpKey = OTP_PREFIX + normalizedEmail;
            redisTemplate.delete(otpKey);

            // Generate new OTP
            String otp = generateOtp();

            // Calculate expiry time
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

            // Create OTP data
            OtpData otpData = new OtpData(normalizedEmail, otp, expiresAt);

            // Store in Redis with TTL
            redisTemplate.opsForValue().set(
                otpKey,
                otpData,
                otpExpiryMinutes,
                TimeUnit.MINUTES
            );

            // Send otp via email
            emailService.sendOtpEmail(email, otp);

            log.info("OTP sent successfully to email: {}", normalizedEmail);


        } catch (Exception e) {
            log.error("Error sending OTP to email: {}", email, e);
        }
    }


    // Verification of OTP
    public Map<String, Object> verifyOtp(String email, String otp) {
        Map<String, Object> response = new HashMap<>();

        try {
            String normalizedEmail = email.toLowerCase().trim();
            String otpKey = OTP_PREFIX + normalizedEmail;

            // Get OTP data from Redis
            Object obj = redisTemplate.opsForValue().get(otpKey);
            OtpData otpData = new ObjectMapper().registerModule(new JavaTimeModule())
                    .convertValue(obj, OtpData.class);


            if (otpData == null) {
                response.put("success", false);
                response.put("message", "No OTP found or OTP has expired. Please request a new one.");
                return response;
            }

            // Check if OTP has expired
            if (LocalDateTime.now().isAfter(otpData.getExpiresAt())) {
                redisTemplate.delete(otpKey);
                response.put("success", false);
                response.put("message", "OTP has expired. Please request a new one.");
                return response;
            }

            // Check max attempts
            if (otpData.getAttempts() >= maxAttempts) {
                redisTemplate.delete(otpKey);
                response.put("success", false);
                response.put("message", "Maximum verification attempts exceeded. Please request a new OTP.");
                return response;
            }

            // OTP verification failed
            if (!otpData.getOtp().equals(otp)) {

                otpData.setAttempts(otpData.getAttempts() + 1);

                //Update in redis
                long timeToLive = redisTemplate.getExpire(otpKey, TimeUnit.MINUTES);
                if(timeToLive > 0) {
                    redisTemplate.opsForValue().set(otpKey, otpData, timeToLive, TimeUnit.MINUTES);
                }

                int remainingAttempts = maxAttempts - otpData.getAttempts();

                response.put("success", false);
                response.put("message", "Invalid OTP. " + remainingAttempts + " attempts remaining.");
                return response;
            }

            // OTP is valid - delete OTP and mark as verified
            redisTemplate.delete(otpKey);

            // Store verified status in Redis (valid for 24 hours) for cache
            String verifiedKey = VERIFIED_PREFIX + normalizedEmail;
            redisTemplate.opsForValue().set(verifiedKey, true, 24, TimeUnit.HOURS);

            response.put("success", true);
            response.put("message", "OTP verified successfully");

            log.info("OTP verified successfully for email: {}", normalizedEmail);

        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}", email, e);
            response.put("success", false);
            response.put("message", "Failed to verify OTP. Please try again.");
        }

        return response;
    }

    // Resend OTP
    public void resendOtp(String email) {
        sendOtp(email);
    }

    // Check if email is verified
    public Boolean isEmailVerified(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        String verifiedKey = VERIFIED_PREFIX + normalizedEmail;
        Boolean verified = (Boolean) redisTemplate.opsForValue().get(verifiedKey);
        return verified != null && verified;
    }

    // Clear verification status (useful for logout or re-verification)
    public void clearVerification(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        String verifiedKey = VERIFIED_PREFIX + normalizedEmail;
        redisTemplate.delete(verifiedKey);
    }

    // Generate random OTP
    private String generateOtp() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int otp = secureRandom.nextInt(max - min + 1) + min;
        return String.valueOf(otp);
    }
}

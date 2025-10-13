package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;


    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "GreenTrack");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP for Verification");

            String htmlContent = buildOtpEmailTemplate(otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP sending to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email", e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailTemplate(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { color: #4CAF50; font-size: 24px; font-weight: bold; margin-bottom: 20px; }
                    .otp-box { 
                        background-color: #f4f4f4; 
                        padding: 20px; 
                        text-align: center; 
                        font-size: 32px; 
                        font-weight: bold; 
                        letter-spacing: 5px; 
                        margin: 20px 0; 
                        border-radius: 5px;
                    }
                    .footer { color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">GreenTrack Email Verification</div>
                    <p>Your One-Time Password (OTP) for verification is:</p>
                    <div class="otp-box">%s</div>
                    <p>This OTP will expire in %d minutes.</p>
                    <p class="footer">If you didn't request this, please ignore this email.</p>
                </div>
            </body>
            </html>
            """.formatted(otp, otpExpiryMinutes);
    }
}

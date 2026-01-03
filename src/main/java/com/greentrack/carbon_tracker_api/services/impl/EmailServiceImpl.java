package com.greentrack.carbon_tracker_api.services.impl;

import com.greentrack.carbon_tracker_api.services.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

//    private final JavaMailSender mailSender;
    private final SendGrid sendGrid;

//    @Value("${spring.mail.username}")
//    private String fromEmail;

    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {

        Email from = new Email(senderEmail, "GreenTrack");
        Email to = new Email(toEmail);

        String subject = "Your OTP for GreenTrack Verification";
        Content content = new Content("text/html", buildOtpEmailTemplate(otp));
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SendGrid OTP email sent to {}", toEmail);
            } else {
                log.error("SendGrid error {}: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid email failed");
            }

        } catch (Exception e) {
            log.error("SendGrid exception", e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }


//    public void sendOtpEmail(String toEmail, String otp) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(fromEmail, "GreenTrack");
//            helper.setTo(toEmail);
//            helper.setSubject("Your OTP for Verification");
//
//            String htmlContent = buildOtpEmailTemplate(otp);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("OTP sending to: {}", toEmail);
//
//        } catch (MessagingException e) {
//            log.error("Failed to send OTP email to: {}", toEmail, e);
//            throw new RuntimeException("Failed to send OTP email", e);
//        } catch (Exception e) {
//            log.error("Unexpected error while sending email", e);
//            throw new RuntimeException("Failed to send OTP email", e);
//        }
//    }

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

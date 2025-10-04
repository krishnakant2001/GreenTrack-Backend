package com.greentrack.carbon_tracker_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String email;
    private String otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer attempts;

    public OtpData(String email, String otp, LocalDateTime expiresAt) {
        this.email = email;
        this.otp = otp;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.attempts = 0;
    }
}

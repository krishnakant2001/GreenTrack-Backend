package com.greentrack.carbon_tracker_api.dto.OtpDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {
    private boolean success;
    private String message;
}

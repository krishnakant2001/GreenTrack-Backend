package com.greentrack.carbon_tracker_api.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "session_storage")
public class Session {

    @Id
    private String id;

    private String sessionId;

    private String refreshToken;

    private LocalDateTime lastUsedAt;

    private String userId;
}

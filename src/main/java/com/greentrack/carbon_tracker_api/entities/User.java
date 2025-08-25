package com.greentrack.carbon_tracker_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotNull
    private String email;

    private String firstName;
    private String lastName;

    @JsonIgnore
    private String passwordHash;

    private String region; // For emission factor calculation (e.g., "IN", "US", "EU")
    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;


    // Soft delete support
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
}

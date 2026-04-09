package com.Benjamin.sentiment_dashboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    private String sessionId;

    private String ipAddress;
    private LocalDateTime joinedAt;
    private LocalDateTime lastSeenAt;
    private Long sessionDurationSeconds;
    private boolean active;
}

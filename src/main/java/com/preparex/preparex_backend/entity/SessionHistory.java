package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.LogoutReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

/**
 * Persistent audit log of all user sessions.
 * Created on login, updated on logout. Not used for active session management (Redis handles that).
 *
 * Edge case: loggedOutAt is null if the session has not been explicitly terminated (e.g., expired via Redis TTL).
 */
@Entity
@Table(
        name = "session_history",
        indexes = {
                @Index(name = "idx_session_history_user_id", columnList = "user_id"),
                @Index(name = "idx_session_history_session_id", columnList = "session_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "logged_in_at", updatable = false)
    private Date loggedInAt;

    @Column(name = "logged_out_at")
    private Date loggedOutAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "logout_reason")
    private LogoutReason logoutReason;
}

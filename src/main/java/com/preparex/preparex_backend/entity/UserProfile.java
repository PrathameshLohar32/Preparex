package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * User profile entity — one-to-one extension of User via shared PK.
 * Stores user-provided profile data (bio, social links, preferences).
 * Auto-created on first profile read via INSERT ON CONFLICT DO NOTHING pattern.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "twitter_url", length = 255)
    private String twitterUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "theme", nullable = false, length = 10)
    @Builder.Default
    private String theme = "LIGHT";

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private Boolean pushNotifications = true;

    @Column(name = "daily_reminder_time", nullable = false, length = 5)
    @Builder.Default
    private String dailyReminderTime = "08:00";

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}

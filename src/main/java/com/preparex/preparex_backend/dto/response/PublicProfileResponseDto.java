package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Public profile response — no email/phone exposed.
 * Used for viewing other users' profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileResponseDto {

    private UUID userId;
    private String name;
    private String username;
    private String bio;
    private String location;
    private String twitterUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String instagramUrl;
    private Integer totalSolved;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer badgesEarned;
    private Integer totalSprints;
    private Integer totalSprintPoints;
}

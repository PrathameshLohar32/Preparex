package com.preparex.preparex_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating user profile.
 * All fields are optional — only non-null fields are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {

    private String bio;
    private String gender;
    private String location;
    private LocalDate dateOfBirth;
    private String twitterUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String instagramUrl;
    private String theme;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String dailyReminderTime;
}

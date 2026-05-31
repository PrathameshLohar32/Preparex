package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for user streak information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakResponseDto {

    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActiveDate;
}

package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a user's rank in the sprint leaderboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintRankDto {

    private int rank;
    private int points;
    private long totalParticipants;
}

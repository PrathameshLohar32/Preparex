package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Leaderboard entry for sprint weekly/monthly rankings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintLeaderboardEntryDto {

    private int rank;
    private UUID userId;
    private String username;
    private int points;
}

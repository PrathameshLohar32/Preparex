package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Contest graph entry for line chart visualization.
 * Shows score/rank progression over time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestGraphEntryDto {

    private String title;
    private int score;
    private int rank;
    private double percentile;
    private Instant date;
}

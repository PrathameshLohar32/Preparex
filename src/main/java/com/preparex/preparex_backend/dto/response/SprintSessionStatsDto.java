package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Running statistics for the current sprint session.
 * Included in answer/skip responses so the client can show real-time progress.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSessionStatsDto {

    private int attempted;
    private int correct;
    private int wrong;
    private int skipped;
    private int points;
    private int skipsRemaining;
}

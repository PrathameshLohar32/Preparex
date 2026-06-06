package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Summary returned when a sprint session ends (either manually or by time expiry).
 * Contains the final scoring breakdown and duration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSummaryDto {

    private UUID sessionId;
    private int totalAttempted;
    private int totalCorrect;
    private int totalWrong;
    private int totalSkipped;
    private int sprintPoints;
    private long durationSecs;
}

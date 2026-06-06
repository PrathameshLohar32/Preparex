package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for sprint session status check.
 * Provides current state, time remaining, and running stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintStatusResponseDto {

    private UUID sessionId;
    private String status;
    private long timeRemainingSecs;
    private int currentQuestionIndex;
    private int totalQuestionsInQueue;
    private ProblemDetailResponseDto currentQuestion;
    private SprintSessionStatsDto sessionStats;
}

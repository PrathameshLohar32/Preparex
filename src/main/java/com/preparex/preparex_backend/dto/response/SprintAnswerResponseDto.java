package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after answering or skipping a sprint question.
 * Contains scoring result, next question, and updated session stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintAnswerResponseDto {

    /** Whether the answer was correct (null for skips) */
    private Boolean correct;

    /** Points awarded for this question (0 for wrong/skip) */
    private int pointsAwarded;

    /** The next question to display (null if session ended) */
    private ProblemDetailResponseDto nextQuestion;

    /** Seconds remaining in the sprint session */
    private long timeRemainingSecs;

    /** Current session running statistics */
    private SprintSessionStatsDto sessionStats;

    /** True if the session auto-ended due to time expiry */
    private boolean sessionEnded;
}

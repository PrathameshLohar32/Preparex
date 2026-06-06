package com.preparex.preparex_backend.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sprint session state stored in Redis for fast access during active sprints.
 * Key: "sprint:session:{sessionId}" with TTL of 35 minutes.
 *
 * <p>This contains the live game state (question queue, current index, skips)
 * that would be too expensive to query from PostgreSQL on every answer/skip request.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSessionState implements Serializable {

    private UUID sessionId;
    private UUID userId;
    private String status;
    private String subjectFilter;
    private String difficultyFilter;

    /** Pre-generated list of problem IDs for the entire sprint */
    @Builder.Default
    private List<UUID> questionQueue = new ArrayList<>();

    /** Index of the current question in the queue (0-based) */
    @Builder.Default
    private int currentIndex = 0;

    /** Remaining skips for this session */
    @Builder.Default
    private int skipsRemaining = 5;

    /** Problems that were skipped and will be recycled back into the queue */
    @Builder.Default
    private List<UUID> skippedQueue = new ArrayList<>();

    /** Timestamp when the session started — used for 30min enforcement */
    private Instant startedAt;

    /** Running total of sprint points */
    @Builder.Default
    private int sprintPoints = 0;

    /** Running count of questions attempted (answered, not skipped) */
    @Builder.Default
    private int attempted = 0;

    /** Running count of correct answers */
    @Builder.Default
    private int correct = 0;

    /** Running count of wrong answers */
    @Builder.Default
    private int wrong = 0;

    /** Running count of skipped questions */
    @Builder.Default
    private int skipped = 0;
}

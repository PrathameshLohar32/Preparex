package com.preparex.preparex_backend.service.contest.result;

import lombok.*;
import java.util.Map;
import java.util.UUID;

/** Holds a participant's total score and breakdown for result computation. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ParticipantScore {
    private UUID userId;
    private int totalScore;
    private int correctCount;
    private int wrongCount;
    private int unattemptedCount;
    private int timeTakenSecs;
    private Map<String, Object> subjectBreakdown;
}

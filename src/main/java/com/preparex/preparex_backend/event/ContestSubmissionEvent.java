package com.preparex.preparex_backend.event;

import lombok.*;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event for async contest submission scoring.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestSubmissionEvent {
    private UUID submissionId;
    private UUID contestId;
    private UUID userId;
    private UUID problemId;
    private Map<String, Object> answer;
    private Integer timeTakenSecs;
}

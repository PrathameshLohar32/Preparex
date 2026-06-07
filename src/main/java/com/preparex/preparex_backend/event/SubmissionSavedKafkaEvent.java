package com.preparex.preparex_backend.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event DTO published when a submission is saved.
 * Consumed by ProfileStatsConsumer to update solved_stats and subject_stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionSavedKafkaEvent {

    private UUID userId;
    private UUID problemId;
    private Integer subjectId;
    private String difficulty;
    private String status;
    private String source;
}

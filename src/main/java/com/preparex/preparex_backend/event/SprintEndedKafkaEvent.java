package com.preparex.preparex_backend.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event DTO published when a sprint session ends.
 * Consumed by SprintStatsConsumer to update user_sprint_stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintEndedKafkaEvent {

    private UUID userId;
    private UUID sessionId;
    private int sprintPoints;
    private Integer weeklyRank;
}

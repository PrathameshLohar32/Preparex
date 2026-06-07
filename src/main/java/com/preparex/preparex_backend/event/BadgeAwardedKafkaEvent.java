package com.preparex.preparex_backend.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Kafka event DTO published when a badge is awarded.
 * Consumed by BadgeConsumer to persist in user_badges.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeAwardedKafkaEvent {

    private UUID userId;
    private String badgeType;
    private String context;
}

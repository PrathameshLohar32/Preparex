package com.preparex.preparex_backend.event;

import lombok.*;
import java.util.UUID;

/**
 * Kafka event published when a contest ends — triggers result finalization.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestEndedEvent {
    private UUID contestId;
}

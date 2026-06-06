package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO returned when a sprint session is successfully started.
 * Contains the session ID, first question, and session metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintStartResponseDto {

    private UUID sessionId;
    private ProblemDetailResponseDto firstQuestion;
    private int totalQuestions;
    private int skipsRemaining;
    private Instant expiresAt;
}

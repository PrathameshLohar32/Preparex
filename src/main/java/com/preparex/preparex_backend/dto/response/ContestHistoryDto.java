package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Contest history entry for profile contest-history endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestHistoryDto {

    private UUID contestId;
    private String contestTitle;
    private Integer totalScore;
    private Integer rank;
    private Double percentile;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unattemptedCount;
    private Instant finalizedAt;
}

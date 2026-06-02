package com.preparex.preparex_backend.dto.response;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestResultResponseDto {
    private UUID contestId;
    private UUID userId;
    private String username;
    private Integer totalScore;
    private Integer rank;
    private Double percentile;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unattemptedCount;
    private Integer timeTakenSecs;
    private Map<String, Object> subjectBreakdown;
}

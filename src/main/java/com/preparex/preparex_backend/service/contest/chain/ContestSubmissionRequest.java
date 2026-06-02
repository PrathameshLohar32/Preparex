package com.preparex.preparex_backend.service.contest.chain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request object passed through the contest submission validation chain.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestSubmissionRequest {
    private UUID contestId;
    private UUID userId;
    private UUID problemId;
    private Map<String, Object> answer;
    private Integer timeTakenSecs;
}

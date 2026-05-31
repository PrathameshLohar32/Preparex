package com.preparex.preparex_backend.service.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing the result of scoring a submission.
 * Returned by all ScoringStrategy implementations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResult {

    private boolean correct;
    private int marksAwarded;
    private String explanation;
}

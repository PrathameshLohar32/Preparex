package com.preparex.preparex_backend.service.strategy;

import com.preparex.preparex_backend.enums.QuestionType;

import java.util.Map;

/**
 * Strategy interface for scoring problem submissions.
 * One implementation per question type.
 *
 * <p>Each implementation is a Spring @Component and is auto-discovered
 * by the ScoringService via the {@link #getSupportedType()} method.</p>
 */
public interface ScoringStrategy {

    /**
     * Returns the question type this strategy handles.
     */
    QuestionType getSupportedType();

    /**
     * Scores a submitted answer against the answer key.
     *
     * @param submittedAnswer the user's answer (from JSONB)
     * @param answerKey       the correct answer (from problem entity — NEVER exposed to client)
     * @return scoring result with correct flag, marks, and explanation
     */
    ScoringResult score(Map<String, Object> submittedAnswer, Map<String, Object> answerKey);
}

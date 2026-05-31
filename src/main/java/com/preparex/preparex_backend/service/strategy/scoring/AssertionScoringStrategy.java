package com.preparex.preparex_backend.service.strategy.scoring;

import com.preparex.preparex_backend.enums.QuestionType;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import com.preparex.preparex_backend.service.strategy.ScoringStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Scoring strategy for ASSERTION (assertion-reason) questions.
 * Evaluates the correctness of assertion, reason, and their relationship.
 * Expected answer_key format: { "correct": "A" | "B" | "C" | "D" }
 * where A=Both true + reason explains, B=Both true + reason doesn't explain,
 * C=Assertion true + reason false, D=Assertion false + reason true.
 */
@Slf4j
@Component
public class AssertionScoringStrategy implements ScoringStrategy {

    private static final int CORRECT_MARKS = 4;

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.ASSERTION;
    }

    @Override
    public ScoringResult score(Map<String, Object> submittedAnswer, Map<String, Object> answerKey) {
        Object submitted = submittedAnswer.get("selected");
        Object correct = answerKey.get("correct");

        if (submitted == null) {
            return ScoringResult.builder()
                    .correct(false)
                    .marksAwarded(0)
                    .explanation("No option selected")
                    .build();
        }

        boolean isCorrect = Objects.equals(submitted.toString(), correct.toString());

        return ScoringResult.builder()
                .correct(isCorrect)
                .marksAwarded(isCorrect ? CORRECT_MARKS : 0)
                .explanation(isCorrect
                        ? "Correct assertion-reason evaluation!"
                        : "Incorrect assertion-reason evaluation")
                .build();
    }
}

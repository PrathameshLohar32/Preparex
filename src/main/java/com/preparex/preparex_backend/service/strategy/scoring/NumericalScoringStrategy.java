package com.preparex.preparex_backend.service.strategy.scoring;

import com.preparex.preparex_backend.enums.QuestionType;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import com.preparex.preparex_backend.service.strategy.ScoringStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Scoring strategy for NUMERICAL questions.
 * Correct if the submitted value falls within the tolerance range
 * defined in answer_key (correct ± tolerance).
 */
@Slf4j
@Component
public class NumericalScoringStrategy implements ScoringStrategy {

    private static final int CORRECT_MARKS = 4;

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.NUMERICAL;
    }

    @Override
    public ScoringResult score(Map<String, Object> submittedAnswer, Map<String, Object> answerKey) {
        Object submittedValue = submittedAnswer.get("value");

        if (submittedValue == null) {
            return ScoringResult.builder()
                    .correct(false)
                    .marksAwarded(0)
                    .explanation("No value submitted")
                    .build();
        }

        double submitted;
        try {
            submitted = Double.parseDouble(submittedValue.toString());
        } catch (NumberFormatException e) {
            return ScoringResult.builder()
                    .correct(false)
                    .marksAwarded(0)
                    .explanation("Invalid numerical value submitted")
                    .build();
        }

        double correct = Double.parseDouble(answerKey.get("correct").toString());
        double tolerance = answerKey.containsKey("tolerance")
                ? Double.parseDouble(answerKey.get("tolerance").toString())
                : 0.0;

        boolean isCorrect = Math.abs(submitted - correct) <= tolerance;

        return ScoringResult.builder()
                .correct(isCorrect)
                .marksAwarded(isCorrect ? CORRECT_MARKS : 0)
                .explanation(isCorrect
                        ? "Correct! Value within tolerance range"
                        : "Incorrect — value outside acceptable range")
                .build();
    }
}

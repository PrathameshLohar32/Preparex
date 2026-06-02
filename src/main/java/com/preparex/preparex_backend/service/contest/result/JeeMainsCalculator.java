package com.preparex.preparex_backend.service.contest.result;

import com.preparex.preparex_backend.entity.ContestProblem;
import com.preparex.preparex_backend.entity.ContestSubmission;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.repository.ContestProblemRepository;
import com.preparex.preparex_backend.repository.ContestRegistrationRepository;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * JEE Mains marking: +4 correct, -1 wrong, 0 unattempted.
 */
@Slf4j
public class JeeMainsCalculator extends ContestResultCalculator {

    public JeeMainsCalculator(
            ContestSubmissionRepository submissionRepository,
            ContestRegistrationRepository registrationRepository,
            ContestProblemRepository problemRepository) {
        super(submissionRepository, registrationRepository, problemRepository);
    }

    @Override
    protected ParticipantScore applyMarkingScheme(
            UUID userId,
            List<ContestSubmission> submissions,
            List<ContestProblem> contestProblems,
            int totalProblems) {

        int totalScore = 0, correct = 0, wrong = 0, totalTime = 0;
        Set<UUID> attemptedProblems = new HashSet<>();

        for (ContestSubmission sub : submissions) {
            attemptedProblems.add(sub.getProblem().getId());
            if (sub.getTimeTakenSecs() != null) totalTime += sub.getTimeTakenSecs();

            if (sub.getStatus() == SubmissionStatus.CORRECT) {
                totalScore += sub.getMarksAwarded() != null ? sub.getMarksAwarded() : 4;
                correct++;
            } else if (sub.getStatus() == SubmissionStatus.WRONG) {
                totalScore += sub.getMarksAwarded() != null ? sub.getMarksAwarded() : -1;
                wrong++;
            }
        }

        return ParticipantScore.builder()
                .userId(userId)
                .totalScore(totalScore)
                .correctCount(correct)
                .wrongCount(wrong)
                .unattemptedCount(totalProblems - attemptedProblems.size())
                .timeTakenSecs(totalTime)
                .subjectBreakdown(Map.of())
                .build();
    }
}

package com.preparex.preparex_backend.service.contest;

import com.preparex.preparex_backend.entity.ContestProblem;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.repository.ContestProblemRepository;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.service.ScoringService;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Contest-specific scoring that applies per-problem marks and negative marking.
 * Reuses ScoringStrategy from Phase 2 for answer evaluation, then applies
 * the contest's marking scheme (marks for correct, negative marks for wrong).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContestScoringService {

    private final ScoringService scoringService;
    private final ProblemRepository problemRepository;
    private final ContestProblemRepository contestProblemRepository;

    /**
     * Scores a contest submission with contest-specific marking.
     *
     * @return ContestScoringResult with status and marks (can be negative)
     */
    public ContestScoringResult score(UUID contestId, UUID problemId, Map<String, Object> answer) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalStateException("Problem not found: " + problemId));

        ContestProblem cp = contestProblemRepository.findByContestIdAndProblemId(contestId, problemId)
                .orElseThrow(() -> new IllegalStateException(
                        "Problem not attached to contest: " + problemId));

        // Score using the reusable strategy engine
        ScoringResult result = scoringService.score(
                problem.getQuestionType(), answer, problem.getAnswerKey());

        SubmissionStatus status;
        int marksAwarded;

        if (result.isCorrect()) {
            status = SubmissionStatus.CORRECT;
            marksAwarded = cp.getMarks();
        } else if (result.getMarksAwarded() > 0) {
            status = SubmissionStatus.PARTIAL;
            marksAwarded = result.getMarksAwarded(); // partial marks from strategy
        } else {
            status = SubmissionStatus.WRONG;
            marksAwarded = -cp.getNegativeMarks(); // negative marking
        }

        log.info("Contest scoring: contest={}, problem={}, status={}, marks={}",
                contestId, problemId, status, marksAwarded);

        return new ContestScoringResult(status, marksAwarded);
    }

    public record ContestScoringResult(SubmissionStatus status, int marksAwarded) {}
}

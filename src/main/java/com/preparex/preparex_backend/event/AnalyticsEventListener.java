package com.preparex.preparex_backend.event;

import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.Submission;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for SubmissionSavedEvent to update denormalized analytics counters
 * on the Problem entity (attempt_count, correct_count).
 * Runs asynchronously to avoid delaying the HTTP response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final ProblemRepository problemRepository;

    @Async
    @EventListener
    @Transactional
    public void handleSubmissionSaved(SubmissionSavedEvent event) {
        Submission submission = event.getSubmission();
        Problem problem = submission.getProblem();

        log.info("Updating analytics for problemId={}, status={}",
                problem.getId(), submission.getStatus());

        problem.setAttemptCount(problem.getAttemptCount() + 1);

        if (submission.getStatus() == SubmissionStatus.CORRECT) {
            problem.setCorrectCount(problem.getCorrectCount() + 1);
        }

        problemRepository.save(problem);

        log.debug("Analytics updated for problemId={}: attempts={}, correct={}",
                problem.getId(), problem.getAttemptCount(), problem.getCorrectCount());
    }
}

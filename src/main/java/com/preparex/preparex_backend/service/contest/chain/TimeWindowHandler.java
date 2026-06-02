package com.preparex.preparex_backend.service.contest.chain;

import com.preparex.preparex_backend.entity.Contest;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.exception.ContestSubmissionException;
import com.preparex.preparex_backend.repository.ContestRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Validates that the contest is LIVE and current time is within the contest window.
 */
@Slf4j
public class TimeWindowHandler extends AbstractSubmissionHandler {

    private final ContestRepository contestRepository;

    public TimeWindowHandler(ContestRepository contestRepository) {
        this.contestRepository = contestRepository;
    }

    @Override
    public void handle(ContestSubmissionRequest request) {
        Contest contest = contestRepository.findById(request.getContestId())
                .orElseThrow(() -> new ContestSubmissionException("Contest not found"));

        if (contest.getStatus() != ContestStatus.LIVE) {
            throw new ContestSubmissionException("Contest is not live. Current status: " + contest.getStatus());
        }

        Instant now = Instant.now();
        if (contest.getStartsAt() != null && now.isBefore(contest.getStartsAt())) {
            throw new ContestSubmissionException("Contest has not started yet");
        }
        if (contest.getEndsAt() != null && now.isAfter(contest.getEndsAt())) {
            throw new ContestSubmissionException("Contest has already ended");
        }

        log.debug("TimeWindowHandler passed for contest={}", request.getContestId());
        passToNext(request);
    }
}

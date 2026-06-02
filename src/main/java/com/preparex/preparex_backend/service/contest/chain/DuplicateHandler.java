package com.preparex.preparex_backend.service.contest.chain;

import com.preparex.preparex_backend.exception.ContestSubmissionException;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates no duplicate submission exists for (contest, user, problem).
 */
@Slf4j
public class DuplicateHandler extends AbstractSubmissionHandler {

    private final ContestSubmissionRepository submissionRepository;

    public DuplicateHandler(ContestSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public void handle(ContestSubmissionRequest request) {
        boolean exists = submissionRepository.existsByContestIdAndUserIdAndProblemId(
                request.getContestId(), request.getUserId(), request.getProblemId());

        if (exists) {
            throw new ContestSubmissionException("Answer already submitted for this problem in this contest");
        }

        log.debug("DuplicateHandler passed for contest={}, user={}, problem={}",
                request.getContestId(), request.getUserId(), request.getProblemId());
        passToNext(request);
    }
}

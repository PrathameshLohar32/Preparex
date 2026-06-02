package com.preparex.preparex_backend.service.contest.chain;

import com.preparex.preparex_backend.exception.ContestSubmissionException;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates that the submitted answer has the correct format (non-null, non-empty).
 */
@Slf4j
public class FormatHandler extends AbstractSubmissionHandler {

    @Override
    public void handle(ContestSubmissionRequest request) {
        if (request.getAnswer() == null || request.getAnswer().isEmpty()) {
            throw new ContestSubmissionException("Answer cannot be null or empty");
        }

        log.debug("FormatHandler passed for contest={}, problem={}",
                request.getContestId(), request.getProblemId());
        passToNext(request);
    }
}

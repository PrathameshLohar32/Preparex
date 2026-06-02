package com.preparex.preparex_backend.service.contest.chain;

import com.preparex.preparex_backend.exception.ContestSubmissionException;
import com.preparex.preparex_backend.repository.ContestRegistrationRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates that the user is registered for the contest.
 */
@Slf4j
public class RegistrationHandler extends AbstractSubmissionHandler {

    private final ContestRegistrationRepository registrationRepository;

    public RegistrationHandler(ContestRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void handle(ContestSubmissionRequest request) {
        boolean registered = registrationRepository
                .existsByContestIdAndUserId(request.getContestId(), request.getUserId());

        if (!registered) {
            throw new ContestSubmissionException("User is not registered for this contest");
        }

        log.debug("RegistrationHandler passed for contest={}, user={}",
                request.getContestId(), request.getUserId());
        passToNext(request);
    }
}

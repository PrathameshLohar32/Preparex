package com.preparex.preparex_backend.service.contest.chain;

import lombok.Setter;

/**
 * Base class for Chain of Responsibility handlers.
 * Provides setNext/passToNext boilerplate.
 */
public abstract class AbstractSubmissionHandler implements ContestSubmissionHandler {

    @Setter
    private ContestSubmissionHandler next;

    protected void passToNext(ContestSubmissionRequest request) {
        if (next != null) {
            next.handle(request);
        }
    }
}

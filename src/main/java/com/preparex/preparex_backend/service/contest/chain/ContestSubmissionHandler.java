package com.preparex.preparex_backend.service.contest.chain;

/**
 * Chain of Responsibility handler for contest submission validation.
 * Each handler validates one aspect and delegates to the next.
 */
public interface ContestSubmissionHandler {
    void handle(ContestSubmissionRequest request);
    void setNext(ContestSubmissionHandler next);
}

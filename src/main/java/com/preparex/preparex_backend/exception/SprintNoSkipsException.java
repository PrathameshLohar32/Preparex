package com.preparex.preparex_backend.exception;

/**
 * Thrown when a user tries to skip a question but has no remaining skips.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class SprintNoSkipsException extends BaseException {

    public SprintNoSkipsException(String message) {
        super(message, "SPRINT_NO_SKIPS_REMAINING");
    }
}

package com.preparex.preparex_backend.exception;

/**
 * Thrown when a sprint session has expired (30-minute limit exceeded).
 * Maps to HTTP 410 Gone.
 */
public class SprintSessionExpiredException extends BaseException {

    public SprintSessionExpiredException(String message) {
        super(message, "SPRINT_SESSION_EXPIRED");
    }
}

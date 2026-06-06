package com.preparex.preparex_backend.exception;

/**
 * Thrown when a sprint session cannot be found (invalid or deleted sessionId).
 * Maps to HTTP 404 Not Found.
 */
public class SprintSessionNotFoundException extends BaseException {

    public SprintSessionNotFoundException(String message) {
        super(message, "SPRINT_SESSION_NOT_FOUND");
    }
}

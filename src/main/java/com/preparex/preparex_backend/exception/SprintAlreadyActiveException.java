package com.preparex.preparex_backend.exception;

/**
 * Thrown when a user attempts to start a sprint while another session is already active.
 * Maps to HTTP 409 Conflict.
 */
public class SprintAlreadyActiveException extends BaseException {

    public SprintAlreadyActiveException(String message) {
        super(message, "SPRINT_ALREADY_ACTIVE");
    }
}

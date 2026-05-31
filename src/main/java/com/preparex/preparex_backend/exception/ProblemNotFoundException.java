package com.preparex.preparex_backend.exception;

/**
 * Thrown when a requested problem is not found or is inactive.
 * Maps to HTTP 404 Not Found.
 */
public class ProblemNotFoundException extends BaseException {

    public ProblemNotFoundException(String message) {
        super(message, "PROBLEM_NOT_FOUND");
    }

    public ProblemNotFoundException() {
        super("Problem not found", "PROBLEM_NOT_FOUND");
    }
}

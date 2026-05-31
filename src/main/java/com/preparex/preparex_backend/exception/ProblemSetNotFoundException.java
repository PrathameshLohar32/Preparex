package com.preparex.preparex_backend.exception;

/**
 * Thrown when a requested problem set is not found or is inactive.
 * Maps to HTTP 404 Not Found.
 */
public class ProblemSetNotFoundException extends BaseException {

    public ProblemSetNotFoundException(String message) {
        super(message, "PROBLEM_SET_NOT_FOUND");
    }

    public ProblemSetNotFoundException() {
        super("Problem set not found", "PROBLEM_SET_NOT_FOUND");
    }
}

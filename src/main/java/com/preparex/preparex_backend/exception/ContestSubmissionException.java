package com.preparex.preparex_backend.exception;

/** Thrown when contest submission validation fails. Maps to 400. */
public class ContestSubmissionException extends BaseException {
    public ContestSubmissionException(String message) { super(message, "CONTEST_SUBMISSION_ERROR"); }
}

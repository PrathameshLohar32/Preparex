package com.preparex.preparex_backend.exception;

/** Thrown for contest state/access violations. Maps to 400/409. */
public class ContestException extends BaseException {
    public ContestException(String message) { super(message, "CONTEST_ERROR"); }
}

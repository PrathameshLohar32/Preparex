package com.preparex.preparex_backend.exception;

public class SessionExpiredException extends BaseException {

    public SessionExpiredException() {
        super("Session has expired. Please log in again", "SESSION_EXPIRED");
    }
}

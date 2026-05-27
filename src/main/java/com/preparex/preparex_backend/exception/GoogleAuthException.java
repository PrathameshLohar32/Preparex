package com.preparex.preparex_backend.exception;

public class GoogleAuthException extends BaseException {

    public GoogleAuthException(String detail) {
        super("Google authentication failed: " + detail, "GOOGLE_AUTH_FAILED");
    }
}

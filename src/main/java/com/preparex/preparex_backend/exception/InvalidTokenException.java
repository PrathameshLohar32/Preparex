package com.preparex.preparex_backend.exception;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String detail) {
        super("Invalid or malformed token: " + detail, "INVALID_TOKEN");
    }
}

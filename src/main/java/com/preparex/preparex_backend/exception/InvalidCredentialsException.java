package com.preparex.preparex_backend.exception;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super("Invalid credentials provided", "INVALID_CREDENTIALS");
    }
}

package com.preparex.preparex_backend.exception;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String detail) {
        super("Unauthorized: " + detail, "UNAUTHORIZED");
    }
}

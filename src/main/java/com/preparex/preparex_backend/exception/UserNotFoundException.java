package com.preparex.preparex_backend.exception;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier, "USER_NOT_FOUND");
    }
}

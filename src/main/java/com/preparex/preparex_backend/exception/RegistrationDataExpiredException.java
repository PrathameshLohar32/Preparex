package com.preparex.preparex_backend.exception;

public class RegistrationDataExpiredException extends BaseException {

    public RegistrationDataExpiredException() {
        super("Registration session has expired. Please restart registration", "REGISTRATION_DATA_EXPIRED");
    }
}

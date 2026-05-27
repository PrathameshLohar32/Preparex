package com.preparex.preparex_backend.exception;

public class InvalidOtpException extends BaseException {

    public InvalidOtpException() {
        super("The OTP provided is invalid", "INVALID_OTP");
    }
}

package com.preparex.preparex_backend.exception;

public class OtpRateLimitExceededException extends BaseException {

    public OtpRateLimitExceededException(String action) {
        super("Too many OTP attempts for action: " + action + ". Please try again later", "OTP_RATE_LIMIT_EXCEEDED");
    }
}

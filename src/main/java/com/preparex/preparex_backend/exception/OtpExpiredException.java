package com.preparex.preparex_backend.exception;

public class OtpExpiredException extends BaseException {

    public OtpExpiredException() {
        super("The OTP has expired. Please request a new one", "OTP_EXPIRED");
    }
}

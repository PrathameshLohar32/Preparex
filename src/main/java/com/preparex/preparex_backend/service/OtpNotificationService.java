package com.preparex.preparex_backend.service;

/**
 * Abstraction for sending OTP notifications (SMS or email).
 * Implementations can be swapped without touching business logic.
 */
public interface OtpNotificationService {

    /**
     * Sends the OTP to the given identifier (phone number or email address).
     * Implementations should detect whether the identifier is phone or email.
     *
     * @param identifier phone or email
     * @param rawOtp     the raw OTP (implement securely — do not log)
     */
    void sendOtp(String identifier, String rawOtp);
}

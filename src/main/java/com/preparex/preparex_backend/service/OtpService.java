package com.preparex.preparex_backend.service;

/**
 * OTP generation, storage, verification, and rate limiting.
 */
public interface OtpService {

    /**
     * Generates an OTP, stores its hash in Redis with TTL, and triggers notification.
     * Enforces resend cooldown — throws OtpRateLimitExceededException if cooldown is active.
     *
     * @param identifier email or phone number
     */
    void sendOtp(String identifier);

    /**
     * Resends an OTP. Enforces resend cooldown.
     *
     * @param identifier email or phone number
     */
    void resendOtp(String identifier);

    /**
     * Verifies the OTP for a given identifier.
     * Increments retry count on failure.
     * Clears OTP data from Redis on success.
     *
     * @throws com.preparex.preparex_backend.exception.OtpExpiredException if OTP not found in Redis
     * @throws com.preparex.preparex_backend.exception.InvalidOtpException if OTP is wrong
     * @throws com.preparex.preparex_backend.exception.OtpRateLimitExceededException if max retries exceeded
     */
    void verifyOtp(String identifier, String rawOtp);
}

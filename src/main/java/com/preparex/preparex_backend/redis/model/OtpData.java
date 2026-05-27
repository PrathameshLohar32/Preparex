package com.preparex.preparex_backend.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Stored in Redis under key: otp:{email|phone}
 * TTL: configured via app.auth.otp.expiry-minutes (default 5 min)
 *
 * Used for standalone OTP send/verify flows (login via phone OTP, etc.)
 * Note: Only the OTP hash is stored, never the raw OTP value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpData implements Serializable {

    private String identifier;
    private String otpHash;
    private Instant createdAt;
    private int retryCount;
    private int resendCount;
}

package com.preparex.preparex_backend.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Stored in Redis under key: temp:reg:{email|phone}
 * TTL: configured via app.auth.registration.temp-data-ttl-minutes (default 10 min)
 *
 * Holds all registration data pending OTP verification.
 * Deleted from Redis upon successful verification and user creation.
 *
 * Note: passwordHash stores the Argon2-hashed password. Raw password is never persisted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempRegistrationData implements Serializable {

    private String name;
    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private String otpHash;
    private Instant otpCreatedAt;
    private int retryCount;
    private int resendCount;
}

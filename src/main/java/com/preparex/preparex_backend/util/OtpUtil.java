package com.preparex.preparex_backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

/**
 * OTP generation and verification utility.
 * Uses BCrypt for OTP hashing since Argon2 is reserved for passwords.
 *
 * Note: Raw OTP values are NEVER logged or stored — only their BCrypt hashes.
 */
@Slf4j
public final class OtpUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);
    private static final int OTP_LENGTH = 6;

    private OtpUtil() {}

    /**
     * Generates a cryptographically secure 6-digit numeric OTP.
     */
    public static String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int otp = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    /**
     * Hashes the OTP using BCrypt.
     */
    public static String hashOtp(String otp) {
        return ENCODER.encode(otp);
    }

    /**
     * Verifies a raw OTP against a stored BCrypt hash.
     */
    public static boolean verifyOtp(String rawOtp, String storedHash) {
        if (rawOtp == null || storedHash == null) {
            return false;
        }
        return ENCODER.matches(rawOtp, storedHash);
    }
}
